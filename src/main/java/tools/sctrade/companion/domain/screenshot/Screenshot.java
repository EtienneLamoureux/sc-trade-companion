package tools.sctrade.companion.domain.screenshot;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Immutable snapshot of a screenshot submitted for processing.
 *
 * @param id Unique identifier.
 * @param image Raw image data.
 * @param location In-game location read from the screenshot, or {@code null} if not yet determined.
 * @param status Current processing status.
 * @param error Human-readable error message, or {@code null} if there is none.
 * @param content Raw OCR content extracted from the image, or {@code null} if not yet extracted.
 * @param type Recognised kiosk type of the screenshot.
 */
public record Screenshot(String id, BufferedImage image, String location, ScreenshotStatus status,
    String error, String content, ScreenshotType type) {

  /** Defensively copies the incoming image so external mutations cannot affect this snapshot. */
  public Screenshot {
    if (image != null) {
      BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
      Graphics2D g = copy.createGraphics();
      g.drawImage(image, 0, 0, null);
      g.dispose();
      image = copy;
    }
  }
}
