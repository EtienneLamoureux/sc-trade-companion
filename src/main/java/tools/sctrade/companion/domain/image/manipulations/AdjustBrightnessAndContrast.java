package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

public class AdjustBrightnessAndContrast implements ImageManipulation {
  private float brightnessFactor;
  private int contrastOffset;

  public AdjustBrightnessAndContrast(float brightnessFactor, int contrastOffset) {
    this.brightnessFactor = brightnessFactor;
    this.contrastOffset = contrastOffset;
  }

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    var copy = ImageUtil.makeCopy(image);
    ImageUtil.adjustBrightnessAndContrast(copy, brightnessFactor, contrastOffset);

    return copy;
  }
}
