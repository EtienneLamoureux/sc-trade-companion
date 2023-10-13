package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

public class WriteToDisk implements ImageManipulation {
  private final Logger logger = LoggerFactory.getLogger(WriteToDisk.class);

  private String path;

  public WriteToDisk(String path) {
    this.path = path;
  }

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    try {
      ImageUtil.writeToDisk(image, path);
    } catch (IOException e) {
      logger.error(
          String.format(Locale.ROOT, "There was an error writing %s to disk", image.toString()), e);
    }

    return image;
  }

}
