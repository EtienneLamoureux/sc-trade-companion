package tools.sctrade.companion.domain.ocr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class OcrResult {
  private Map<Double, LocatedLine> linesByY;

  public OcrResult() {
    linesByY = new TreeMap<>();
  }

  public String getText() {
    return getParagraphs().stream()
        .map(paragraph -> paragraph.stream().map(line -> line.getText())
            .collect(Collectors.joining(System.lineSeparator())))
        .collect(Collectors.joining("\n\n"));
  }

  public void add(LocatedWord word) {
    var line = linesByY.values().stream().filter(n -> n.shouldContain(word)).findFirst();

    if (line.isPresent()) {
      line.get().add(word);
    } else {
      var newLine = new LocatedLine(word);
      linesByY.put(newLine.getBoundingBox().getCenterY(), newLine);
    }
  }

  private List<List<LocatedLine>> getParagraphs() {
    var paragraphs = new ArrayList<List<LocatedLine>>();
    LocatedLine previousLine = null;

    for (var line : linesByY.values()) {
      if (previousLine == null || previousLine.isSeparatedFrom(line)) {
        paragraphs.add(new ArrayList<>());
      }

      paragraphs.get(paragraphs.size() - 1).add(line);
      previousLine = line;
    }

    return paragraphs;
  }
}
