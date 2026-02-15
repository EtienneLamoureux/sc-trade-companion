package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Finds and crops to the largest rectangle in an image. This is useful after perspective correction
 * to remove any black borders or unwanted areas around the main content.
 */
public class LargestRectangleCrop implements ImageManipulation {
  @Override
  public BufferedImage manipulate(BufferedImage image) {
    return ImageUtil.cropToLargestRectangle(image);
  }
}
