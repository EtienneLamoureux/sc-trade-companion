package tools.sctrade.companion.domain.image;

import java.awt.image.BufferedImage;

public interface ImageWriter {
  void write(BufferedImage image, ImageType type);
}
