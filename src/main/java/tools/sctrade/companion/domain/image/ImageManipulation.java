package tools.sctrade.companion.domain.image;

import java.awt.image.BufferedImage;

/**
 * This interface is used to define image manipulation operations.
 */
public interface ImageManipulation {
  BufferedImage manipulate(BufferedImage image);
}
