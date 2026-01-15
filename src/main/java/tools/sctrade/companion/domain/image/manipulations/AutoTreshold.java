package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Thresholds (converts to pure black and white) an image automatically.
 */
public class AutoTreshold implements ImageManipulation {
  @Override
  public BufferedImage manipulate(BufferedImage image) {
    return ImageUtil.applyOtsuBinarization(image);
  }
}
