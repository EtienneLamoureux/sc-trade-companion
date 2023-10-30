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
import tools.sctrade.companion.domain.image.ImageProcessor;
import tools.sctrade.companion.utils.ImageUtil;

public class ScreenPrinter implements Runnable {
  private final Logger logger = LoggerFactory.getLogger(ScreenPrinter.class);

  private Collection<ImageProcessor> imageProcessors;
  private List<ImageManipulation> postprocessingManipulations;

  public ScreenPrinter(Collection<ImageProcessor> imageProcessors) {
    this(imageProcessors, Collections.emptyList());
  }

  public ScreenPrinter(Collection<ImageProcessor> imageProcessors,
      List<ImageManipulation> postprocessingManipulations) {
    this.imageProcessors = imageProcessors;
    this.postprocessingManipulations = postprocessingManipulations;
  }

  @Override
  public void run() {
    try {
      var screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
      var screenCapture = postProcess(new Robot().createScreenCapture(screenRectangle));

      imageProcessors.stream().forEach(n -> n.processAsynchronously(screenCapture));

      ImageUtil.writeToDisk(screenCapture);
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
