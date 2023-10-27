package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

public class AdjustBrightnessAndContrast implements ImageManipulation {
  private float contrastScale;
  private float brightnessOffset;

  public AdjustBrightnessAndContrast(float contrastScale, float brightnessOffset) {
    this.contrastScale = contrastScale;
    this.brightnessOffset = brightnessOffset;
  }

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    var copy = ImageUtil.makeCopy(image);
    ImageUtil.adjustBrightnessAndContrast(copy, contrastScale, brightnessOffset);

    return copy;
  }
}
