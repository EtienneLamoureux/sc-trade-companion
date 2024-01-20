package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

public class CommodityKioskTextThreshold implements ImageManipulation {

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    return ImageUtil.applyAdaptiveGaussianThreshold(image, 41, 14);
  }
}
