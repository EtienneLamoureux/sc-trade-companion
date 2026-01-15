package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Converts an image to histogram-equalized greyscale.
 */
public class ConvertToEqualizedGreyscale implements ImageManipulation {
  @Override
  public BufferedImage manipulate(BufferedImage image) {
    return ImageUtil.makeClaheEqualizedGreyscaleCopy(image);
  }
}
