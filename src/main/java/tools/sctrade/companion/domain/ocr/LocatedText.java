package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;

/**
 * Represents a piece of text as read by the OCR. This class is used to represent both individual
 * words and larger text fragments. Encapsulates the coordinates of the text and the text itself.
 */
public abstract class LocatedText {
  protected Rectangle boundingBox;

  /**
   * Gets the text of the located text, as it would be read by a human.
   *
   * @return The text of the located text.
   */
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
