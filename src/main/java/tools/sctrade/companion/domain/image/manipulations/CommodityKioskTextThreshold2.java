package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Applies a medium adaptive Gaussian threshold to an image.
 */
public class CommodityKioskTextThreshold2 implements ImageManipulation {

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    return ImageUtil.applyAdaptiveGaussianThreshold(image, 41, 20);
  }
}
