package tools.sctrade.companion.domain.image;

import java.awt.image.BufferedImage;

/**
 * This interface is used to define image manipulation operations.
 */
public interface ImageManipulation {
  /**
   * Manipulates the given image. May process the provided image in-plae, modifying it.
   *
   * @param image Image to manipulate.
   * @return Manipulated image.
   */
  BufferedImage manipulate(BufferedImage image);
}
