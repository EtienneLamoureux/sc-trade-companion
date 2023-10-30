package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

public class UpscaleTo4k implements ImageManipulation {
  private static final int TARGET_HEIGHT = 2196;

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    if (image.getHeight() >= TARGET_HEIGHT) {
      return image;
    }

    return ImageUtil.scaleToHeight(image, TARGET_HEIGHT);
  }

}
