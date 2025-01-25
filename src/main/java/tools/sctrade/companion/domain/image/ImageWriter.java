package tools.sctrade.companion.domain.image;

import java.awt.image.BufferedImage;

/**
 * Interface for writing images to disk. To be implemented by an concrete output port.
 */
public interface ImageWriter {
  void write(BufferedImage image, ImageType type);
}
