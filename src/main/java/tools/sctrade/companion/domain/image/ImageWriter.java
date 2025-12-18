package tools.sctrade.companion.domain.image;

import java.awt.image.BufferedImage;

/**
 * Interface for writing images to disk. To be implemented by an concrete output port.
 */
public interface ImageWriter<T> {
  /**
   * Writes the image to disk.
   *
   * @param image The image to write.
   * @param type The type of the image.
   * 
   * @return Output type, as defined by the implementation
   */
  T write(BufferedImage image, ImageType type);
}
