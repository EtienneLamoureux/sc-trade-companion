package tools.sctrade.companion.input;

import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.screenshot.ScreenshotRepository;
import tools.sctrade.companion.domain.screenshot.ScreenshotType;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.utils.GraphicsDeviceUtil;
import tools.sctrade.companion.utils.LocalizationUtil;
import tools.sctrade.companion.utils.SoundUtil;

/**
 * Runnable that captures the configured screen and processes it. Plays a sound when doing so.
 */
public class ScreenPrinter extends ScreenshotProducer implements Runnable {
  private static final String CAMERA_SHUTTER = "/sounds/camera-shutter.wav";

  private final Logger logger = LoggerFactory.getLogger(ScreenPrinter.class);

  private List<ImageManipulation> postprocessingManipulations;
  private ImageWriter<Optional<Path>> imageWriter;
  private SoundUtil soundPlayer;
  private NotificationService notificationService;
  private SettingRepository settings;

  /**
   * Creates a new instance of the screen printer.
   *
   * @param screenshotRepository The repository used to persist screenshot records.
   * @param screenshotType The type of screenshot this printer produces.
   * @param imageWriter The image writer to save the screen capture.
   * @param soundPlayer The sound player to play a sound when capturing the screen.
   * @param notificationService The notification service to notify the user of the screen capture
   * @param settings The settings repository.
   */
  public ScreenPrinter(BlockingQueue<BufferedImage> queue,
      ScreenshotRepository screenshotRepository, ScreenshotType screenshotType,
      ImageWriter<Optional<Path>> imageWriter, SoundUtil soundPlayer,
      NotificationService notificationService, SettingRepository settings) {
    this(queue, screenshotRepository, screenshotType, Collections.emptyList(), imageWriter,
        soundPlayer, notificationService, settings);
  }

  /**
   * Creates a new instance of the screen printer.
   *
   * @param screenshotRepository The repository used to persist screenshot records.
   * @param screenshotType The type of screenshot this printer produces.
   * @param postprocessingManipulations The postprocessing manipulations to apply to the screen
   *        capture, after capturing it but before handing it over to the image processors.
   * @param imageWriter The image writer to save the screen capture.
   * @param soundPlayer The sound player to play a sound when capturing the screen.
   * @param notificationService The notification service to notify the user of the screen capture
   * @param settings The settings repository.
   */
  public ScreenPrinter(BlockingQueue<BufferedImage> queue,
      ScreenshotRepository screenshotRepository, ScreenshotType screenshotType,
      List<ImageManipulation> postprocessingManipulations, ImageWriter<Optional<Path>> imageWriter,
      SoundUtil soundPlayer, NotificationService notificationService, SettingRepository settings) {
    super(queue, screenshotRepository, screenshotType);
    this.postprocessingManipulations = postprocessingManipulations;
    this.imageWriter = imageWriter;
    this.soundPlayer = soundPlayer;
    this.notificationService = notificationService;
    this.settings = settings;
  }

  @Override
  public void run() {
    try {
      logger.debug("Printing screen...");
      soundPlayer.play(CAMERA_SHUTTER);
      var monitor = GraphicsDeviceUtil
          .get(settings.get(Setting.STAR_CITIZEN_MONITOR, GraphicsDeviceUtil.getPrimaryId()));
      var screenRectangle = monitor.getDefaultConfiguration().getBounds();
      var screenCapture = postProcess(new Robot(monitor).createScreenCapture(screenRectangle));
      logger.debug("Printed screen");

      logger.debug("Queueing image...");
      produce(screenCapture);
      logger.debug("Queued image");
      notificationService.info(LocalizationUtil.get("infoProcessingScreenshot"));

      imageWriter.write(screenCapture, ImageType.SCREENSHOT);
    } catch (Exception e) {
      logger.error("Error while printing screen", e);
    }
  }

  private BufferedImage postProcess(BufferedImage screenCapture) {
    for (var manipulation : postprocessingManipulations) {
      screenCapture = manipulation.manipulate(screenCapture);
    }

    return screenCapture;
  }

}
