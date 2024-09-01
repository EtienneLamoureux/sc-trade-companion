package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;

public abstract class LocatedText {
  protected Rectangle boundingBox;

  public abstract String getText();

  public Rectangle getBoundingBox() {
    return boundingBox;
  }

  public double getCharacterWidth() {
    return boundingBox.getWidth() / getText().length();
  }

  @Override
  public String toString() {
    return getText();
  }
}
