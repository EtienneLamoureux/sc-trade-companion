package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

class LocatedLine {
  private Map<Double, LocatedWord> wordsByX;
  private Rectangle boundingBox;

  LocatedLine(LocatedWord word) {
    wordsByX = new TreeMap<>();
    boundingBox = null;

    add(word);
  }

  public Rectangle getBoundingBox() {
    return boundingBox;
  }

  String getText() {
    return wordsByX.values().stream().map(n -> n.getText()).collect(Collectors.joining(" "));
  }

  boolean shouldContain(LocatedWord word) {
    return boundingBox.getMaxY() > word.getBoundingBox().getCenterY()
        && word.getBoundingBox().getCenterY() > boundingBox.getMinY();
  }

  void add(LocatedWord word) {
    wordsByX.put(word.getBoundingBox().getCenterX(), word);

    if (boundingBox != null) {
      boundingBox.add(word.getBoundingBox());
    } else
      boundingBox = new Rectangle(word.getBoundingBox());
  }

  boolean isSeparatedFrom(LocatedLine line) {
    return Math.abs(getBoundingBox().getCenterY() - line.getBoundingBox().getCenterY()) > (2
        * Math.min(getBoundingBox().height, line.getBoundingBox().height));
  }
}
