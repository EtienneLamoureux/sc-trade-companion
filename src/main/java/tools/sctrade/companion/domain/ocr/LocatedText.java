package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.util.regex.Pattern;

/**
 * Represents a piece of text as read by the OCR. This class is used to represent both individual
 * words and larger text fragments. Encapsulates the coordinates of the text and the text itself.
 */
public abstract class LocatedText {
  private static final Pattern NUMERICAL_PATTERN = Pattern.compile("\\d+");

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

  public boolean isContainedBy(Rectangle boundingBox) {
    return boundingBox.contains(this.boundingBox);
  }

  @Override
  public String toString() {
    return getText();
  }

  protected boolean isNumerical() {
    return NUMERICAL_PATTERN.matcher(getText()).find();
  }
}
