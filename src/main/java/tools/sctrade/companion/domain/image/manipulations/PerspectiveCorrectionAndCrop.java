package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;
import tools.sctrade.companion.utils.ImageUtil.PerspectiveCorrectionResult;

/**
 * Applies perspective correction and crops to the transformed rectangle in one step. This combines
 * perspective correction with automatic cropping to the rectangle used for the transformation,
 * avoiding the need for separate black border detection.
 */
public class PerspectiveCorrectionAndCrop implements ImageManipulation {
  @Override
  public BufferedImage manipulate(BufferedImage image) {
    PerspectiveCorrectionResult result = ImageUtil.applyPerspectiveCorrection(image);
    // If perspective correction was applied (rectangle is not null), crop to that rectangle
    // Otherwise, just return the image (which is the original if no quad was found)
    if (result.rectangle() != null) {
      return ImageUtil.cropToLargestRectangle(result.image(), result.rectangle());
    }
    return result.image();
  }
}
