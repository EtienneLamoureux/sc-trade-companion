package tools.sctrade.companion.domain;

import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

public abstract class ImageProcessor {
  private final Logger logger = LoggerFactory.getLogger(ImageProcessor.class);

  @Async
  public final void processAsynchronously(BufferedImage image) {
    try {
      process(image);
    } catch (Exception e) {
      logger.error("Error while processing image", e);
    }
  }

  protected abstract void process(BufferedImage image);
}
