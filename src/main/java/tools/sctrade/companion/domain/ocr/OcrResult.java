package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import tools.sctrade.companion.utils.MathUtil;

/**
 * Text, as read and located by the OCR.
 */
public class OcrResult {
  private Map<Double, LocatedLine> linesByY;
  private Map<Double, LocatedColumn> columnsByX;

  /**
   * Creates a new OCR result.
   *
   * @param words The words that were read.
   */
  public OcrResult(Collection<LocatedWord> words) {
    linesByY = new TreeMap<>();
    columnsByX = new TreeMap<>();

    for (var word : words) {
      add(word);
    }

    buildColumns();
  }

  public List<LocatedLine> getLines() {
    return linesByY.values().stream().toList();
  }

  public String getTextByLines() {
    return linesByY.values().stream().map(n -> n.getText())
        .collect(Collectors.joining(System.lineSeparator()));
  }

  public List<LocatedColumn> getColumns() {
    return columnsByX.values().stream().toList();
  }

  public String getTextByColumns() {
    return columnsByX.values().stream().map(n -> n.getText()).collect(Collectors.joining("\n\n"));
  }

  public List<LocatedColumn> getTwoColumns() {
    var xGapCenters =
        linesByY.values().stream().map(n -> n.getXGapCenters()).flatMap(n -> n.stream()).toList();
    var meanXGapCenter = MathUtil.calculateMean(xGapCenters);

    var leftColumn = new LocatedColumn();
    var rightColumn = new LocatedColumn();

    for (var fragment : getFragments()) {
      if (fragment.getBoundingBox().getCenterX() > meanXGapCenter) {
        leftColumn.add(fragment);
      } else {
        rightColumn.add(fragment);
      }
    }

    return List.of(leftColumn, rightColumn);
  }

  /**
   * Creates a shallow copy of this object, containing only the {@link LocatedText} fully contained
   * within the bounding box.
   *
   * @param boundingBox Rectangle
   * @return OcrResult
   */
  public OcrResult crop(Rectangle boundingBox) {
    var wordsInBoundingBox = linesByY.values().stream().map(n -> n.getFragments())
        .flatMap(n -> n.stream()).map(n -> n.getWordsInReadingOrder()).flatMap(n -> n.stream())
        .filter(n -> n.isContainedBy(boundingBox)).toList();

    return new OcrResult(wordsInBoundingBox);
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

  private void buildColumns() {
    var fragments = getFragments();
    Collections.reverse(fragments);

    for (var fragment : fragments) {
      var column = columnsByX.values().stream().filter(n -> n.shouldContain(fragment)).findFirst();

      if (column.isPresent()) {
        column.get().add(fragment);
      } else {
        var newColumn = new LocatedColumn(fragment);
        columnsByX.put(newColumn.getBoundingBox().getCenterX(), newColumn);
      }
    }
  }

  private List<LocatedFragment> getFragments() {
    return linesByY.values().stream().flatMap(n -> n.getFragments().stream())
        .collect(Collectors.toList());
  }
}
