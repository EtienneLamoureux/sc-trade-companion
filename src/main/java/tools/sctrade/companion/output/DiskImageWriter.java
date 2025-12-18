package tools.sctrade.companion.output;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Writes images to disk.
 */
public class DiskImageWriter implements ImageWriter<Optional<Path>> {
  private final Logger logger = LoggerFactory.getLogger(DiskImageWriter.class);

  private SettingRepository settings;

  /**
   * Creates a new instance of the disk image writer.
   *
   * @param settings The settings repository.
   */
  public DiskImageWriter(SettingRepository settings) {
    this.settings = settings;
  }

  @Override
  public Optional<Path> write(BufferedImage image, ImageType type) {
    if (!shouldWrite(type)) {
      logger.debug("Configured to not write images of type {}, skipping", type);
      return Optional.empty();
    }

    Path path = Paths.get(settings.get(Setting.MY_IMAGES_PATH).toString(), type.generateFileName());
    logger.info("Writing '{}' to disk...", path);
    ImageUtil.writeToDiskNoFail(image, path);

    return Optional.of(path);
  }

  private boolean shouldWrite(ImageType type) {
    switch (type) {
      case SCREENSHOT:
        return settings.get(Setting.OUTPUT_SCREENSHOTS);
      case BUTTONS, BUY_BUTTON, SELL_BUTTON, PREPROCESSED:
        return settings.get(Setting.OUTPUT_TRANSIENT_IMAGES);
      default:
        return false;
    }
  }
}
