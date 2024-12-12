package tools.sctrade.companion.input;

import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.utils.AsynchronousProcessor;
import tools.sctrade.companion.utils.GraphicsDeviceUtil;
import tools.sctrade.companion.utils.LocalizationUtil;
import tools.sctrade.companion.utils.SoundUtil;

public class ScreenPrinter implements Runnable {
  private static final String CAMERA_SHUTTER = "/sounds/camera-shutter.wav";

  private final Logger logger = LoggerFactory.getLogger(ScreenPrinter.class);

  private Collection<AsynchronousProcessor<BufferedImage>> imageProcessors;
  private List<ImageManipulation> postprocessingManipulations;
  private ImageWriter imageWriter;
  private SoundUtil soundPlayer;
  private NotificationService notificationService;
  private SettingRepository settings;

  public ScreenPrinter(Collection<AsynchronousProcessor<BufferedImage>> imageProcessors,
      ImageWriter imageWriter, SoundUtil soundPlayer, NotificationService notificationService,
      SettingRepository settings) {
    this(imageProcessors, Collections.emptyList(), imageWriter, soundPlayer, notificationService,
        settings);
  }

  public ScreenPrinter(Collection<AsynchronousProcessor<BufferedImage>> imageProcessors,
      List<ImageManipulation> postprocessingManipulations, ImageWriter imageWriter,
      SoundUtil soundPlayer, NotificationService notificationService, SettingRepository settings) {
    this.imageProcessors = imageProcessors;
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

      logger.debug("Calling image processors...");
      imageProcessors.stream().forEach(n -> n.processAsynchronously(screenCapture));
      logger.debug("Called image processors");
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
