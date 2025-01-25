package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Inverts the colors of an image.
 */
public class InvertColors implements ImageManipulation {
  @Override
  public BufferedImage manipulate(BufferedImage image) {
    var copy = ImageUtil.makeCopy(image);
    ImageUtil.invertColors(copy);

    return copy;
  }
}
