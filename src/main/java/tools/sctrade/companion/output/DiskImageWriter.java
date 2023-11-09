package tools.sctrade.companion.output;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.utils.ImageUtil;

public class DiskImageWriter implements ImageWriter {
  private final Logger logger = LoggerFactory.getLogger(ImageWriter.class);

  private Path basePath;
  private boolean debugScreenshots;
  private boolean debugIntermediaryImages;

  public DiskImageWriter(Path basePath, boolean debugScreenshots, boolean debugIntermediaryImages) {
    this.basePath = basePath.toAbsolutePath();
    this.debugScreenshots = debugScreenshots;
    this.debugIntermediaryImages = debugIntermediaryImages;
  }

  @Override
  public void write(BufferedImage image, ImageType type) {
    if (!shouldWrite(type)) {
      logger.debug("Configured to not write images of type {}, skipping", type.toString());
      return;
    }

    Path path = Paths.get(basePath.toString(), type.generateFileName());
    logger.info("Writing '{}' to disk...", path.toString());
    ImageUtil.writeToDiskNoFail(image, path);
  }

  private boolean shouldWrite(ImageType type) {
    switch (type) {
      case SCREENSHOT:
        return debugScreenshots;
      case BUY_BUTTON, SELL_BUTTON, PREPROCESSED:
        return debugIntermediaryImages;
      default:
        return false;
    }
  }
}
