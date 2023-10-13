package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

public class ScaleAndOffsetColors implements ImageManipulation {
  private float scale;
  private float offset;

  public ScaleAndOffsetColors(float scale, float offset) {
    this.scale = scale;
    this.offset = offset;
  }

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    var copy = ImageUtil.makeCopy(image);
    ImageUtil.scaleAndOffsetColors(copy, scale, offset);

    return copy;
  }
}
