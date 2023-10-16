package tools.sctrade.companion.domain.ocr;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class OcrResult {
  private Map<Double, LocatedLine> linesByY;

  public OcrResult(Collection<LocatedWord> words) {
    linesByY = new TreeMap<>();

    for (var word : words) {
      add(word);
    }
  }

  public String getText() {
    return linesByY.values().stream().map(line -> line.getText())
        .collect(Collectors.joining(System.lineSeparator()));
  }

  public Collection<LocatedFragment> getFragments() {
    return linesByY.values().stream().flatMap(n -> n.getFragments().stream()).toList();
  }

  private void add(LocatedWord word) {
    var line = linesByY.values().stream().filter(n -> n.shouldContain(word)).findFirst();

    if (line.isPresent()) {
      line.get().add(word);
    } else {
      var newLine = new LocatedLine(word);
      linesByY.put(newLine.getBoundingBox().getCenterY(), newLine);
    }
  }
}
