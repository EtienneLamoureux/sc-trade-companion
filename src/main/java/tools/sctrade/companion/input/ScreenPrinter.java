package tools.sctrade.companion.input;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.utils.AsynchronousProcessor;
import tools.sctrade.companion.utils.SoundUtil;

public class ScreenPrinter implements Runnable {
  private static final String CAMERA_SHUTTER = "/sounds/camera-shutter.wav";

  private final Logger logger = LoggerFactory.getLogger(ScreenPrinter.class);

  private Collection<AsynchronousProcessor<BufferedImage>> imageProcessors;
  private List<ImageManipulation> postprocessingManipulations;
  private ImageWriter imageWriter;
  private SoundUtil soundPlayer;

  public ScreenPrinter(Collection<AsynchronousProcessor<BufferedImage>> imageProcessors,
      ImageWriter imageWriter, SoundUtil soundPlayer) {
    this(imageProcessors, Collections.emptyList(), imageWriter, soundPlayer);
  }

  public ScreenPrinter(Collection<AsynchronousProcessor<BufferedImage>> imageProcessors,
      List<ImageManipulation> postprocessingManipulations, ImageWriter imageWriter,
      SoundUtil soundPlayer) {
    this.imageProcessors = imageProcessors;
    this.postprocessingManipulations = postprocessingManipulations;
    this.imageWriter = imageWriter;
    this.soundPlayer = soundPlayer;
  }

  @Override
  public void run() {
    try {
      logger.debug("Printing screen...");
      soundPlayer.play(CAMERA_SHUTTER);
      var screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
      var screenCapture = postProcess(new Robot().createScreenCapture(screenRectangle));
      logger.debug("Printed screen");

      logger.debug("Calling image processors...");
      imageProcessors.stream().forEach(n -> n.processAsynchronously(screenCapture));
      logger.debug("Called image processors");

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
