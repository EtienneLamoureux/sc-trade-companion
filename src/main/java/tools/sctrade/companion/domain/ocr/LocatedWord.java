package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;

public class LocatedWord extends LocatedText {
  private String text;

  public LocatedWord(String text, Rectangle boundingBox) {
    this.text = text.strip();
    this.boundingBox = boundingBox;
  }

  @Override
  public String getText() {
    return text;
  }

  public boolean isSeparatedFrom(LocatedText word) {
    double maxCharacterWidth = Math.max(getCharacterWidth(), word.getCharacterWidth());
    double leeway = 2 * maxCharacterWidth;
    double distanceBetweenWords = Math.abs(boundingBox.getMaxX() - word.getBoundingBox().getMinX());

    return distanceBetweenWords > leeway;
  }
}
