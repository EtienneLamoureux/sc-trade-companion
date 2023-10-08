package tools.sctrade.companion.domain;

import java.awt.image.BufferedImage;
import org.springframework.scheduling.annotation.Async;

public interface ImageProcessor {
  @Async
  void processAsynchronously(BufferedImage screenCapture);
}
