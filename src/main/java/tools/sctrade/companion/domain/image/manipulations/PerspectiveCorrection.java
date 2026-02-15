package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Applies perspective correction to an image by detecting the largest quadrilateral and
 * straightening it. This is useful for correcting skewed or tilted rectangular objects like screens
 * or documents.
 */
public class PerspectiveCorrection implements ImageManipulation {
  @Override
  public BufferedImage manipulate(BufferedImage image) {
    return ImageUtil.applyPerspectiveCorrection(image).image();
  }
}
