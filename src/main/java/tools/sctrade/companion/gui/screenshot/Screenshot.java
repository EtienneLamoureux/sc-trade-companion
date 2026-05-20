package tools.sctrade.companion.gui.screenshot;

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

  /**
   * Returns a new {@link Screenshot} that merges {@code update} into this record.
   *
   * <p>
   * Each field of {@code update} replaces the corresponding field of this record when the update
   * value is non-{@code null}; otherwise the existing value is kept. The {@code id} must match.
   *
   * @param update Partial update to apply. Must carry the same {@code id} as this record.
   * @return A fresh {@link Screenshot} with the merged field values.
   * @throws IllegalArgumentException if {@code update.id()} differs from this record's {@code id}.
   */
  public Screenshot updateUsing(Screenshot update) {
    if (!this.id.equals(update.id())) {
      throw new IllegalArgumentException(
          "Cannot update screenshot '%s' using screenshot with different id '%s'".formatted(this.id,
              update.id()));
    }

    return new Screenshot(this.id, update.image() != null ? update.image() : this.image,
        update.location() != null ? update.location() : this.location,
        update.status() != null ? update.status() : this.status,
        update.error() != null ? update.error() : this.error,
        update.content() != null ? update.content() : this.content,
        update.type() != null ? update.type() : this.type);
  }
}
