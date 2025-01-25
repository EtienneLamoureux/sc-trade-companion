package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Adjusts the brightness and contrast of an image.
 */
public class AdjustBrightnessAndContrast implements ImageManipulation {
  private float contrastScale;
  private float brightnessOffset;

  /**
   * Creates a new instance of AdjustBrightnessAndContrast.
   *
   * @param contrastScale The scale of the contrast.
   * @param brightnessOffset The offset of the brightness.
   */
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
