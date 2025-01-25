package tools.sctrade.companion.domain.image;

import java.awt.image.BufferedImage;

/**
 * Interface for writing images to disk. To be implemented by an concrete output port.
 */
public interface ImageWriter {
  /**
   * Writes the image to disk.
   *
   * @param image The image to write.
   * @param type The type of the image.
   */
  void write(BufferedImage image, ImageType type);
}
