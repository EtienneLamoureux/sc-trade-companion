package tools.sctrade.companion.domain.image;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.utils.ImageUtil;

public abstract class Ocr {
  private OcrConfiguration configuration;

  public Ocr(OcrConfiguration configuration) {
    this.configuration = configuration;
  }

  public final String read(BufferedImage image) {
    image = preProcess(image);

    return process(image);
  }

  protected abstract String process(BufferedImage image);

  private BufferedImage preProcess(BufferedImage image) {
    if (configuration.convertToGreyscale()) {
      image = ImageUtil.convertToGreyscale(image);
    }

    if (configuration.invertColors()) {
      ImageUtil.invertColors(image);
    }

    if (configuration.shouldRescale()) {
      ImageUtil.adjustBrightnessAndContrast(image, configuration.brightnessFactor(),
          configuration.contrastOffset());
    }

    return image;
  }
}
