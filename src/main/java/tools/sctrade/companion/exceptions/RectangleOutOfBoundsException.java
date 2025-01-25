package tools.sctrade.companion.exceptions;

import java.awt.Rectangle;
import java.util.Locale;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Thrown when a {@link Rectangle} is out of bounds of an image.
 */
public class RectangleOutOfBoundsException extends RuntimeException {
  private static final long serialVersionUID = 8627902304541564806L;

  /**
   * Constructs a new RectangleOutOfBoundsException
   * 
   * @param rectangle The rectangle that is out of bounds
   * @param imageRectangle The image rectangle that the rectangle is out of bounds of
   */
  public RectangleOutOfBoundsException(Rectangle rectangle, Rectangle imageRectangle) {
    super(String.format(Locale.ROOT, LocalizationUtil.get("errorRectangleOutOfBounds"),
        rectangle.toString(), imageRectangle.toString()));
  }

}
