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
}
