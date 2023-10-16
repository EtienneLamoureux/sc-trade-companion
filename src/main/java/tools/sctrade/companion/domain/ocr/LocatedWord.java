package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;

public class LocatedWord {
  private String text;
  private Rectangle boundingBox;

  public LocatedWord(String text, Rectangle boundingBox) {
    this.text = text.strip();
    this.boundingBox = boundingBox;
  }

  public String getText() {
    return text;
  }

  public Rectangle getBoundingBox() {
    return boundingBox;
  }

  public boolean isSeparatedFrom(LocatedWord word) {
    return (Math.abs(boundingBox.getMaxX()) - Math.abs(word.getBoundingBox().getMinX())) >= (2
        * Math.max((boundingBox.getWidth() / text.length()),
            (word.getBoundingBox().getWidth() / word.getText().length())));
  }
}
