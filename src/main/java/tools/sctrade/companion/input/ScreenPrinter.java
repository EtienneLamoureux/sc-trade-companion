package tools.sctrade.companion.input;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageProcessor;
import tools.sctrade.companion.utils.ImageUtil;

public class ScreenPrinter implements Runnable {
  private final Logger logger = LoggerFactory.getLogger(ScreenPrinter.class);

  private Collection<ImageProcessor> imageProcessors;

  public ScreenPrinter(Collection<ImageProcessor> imageProcessors) {
    this.imageProcessors = imageProcessors; // TODO
  }

  @Override
  public void run() {
    try {
      var screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
      var screenCapture = new Robot().createScreenCapture(screenRectangle);

      imageProcessors.stream().forEach(n -> n.processAsynchronously(screenCapture));

      ImageUtil.writeToDisk(screenCapture);
    } catch (Exception e) {
      logger.error("Error while printing screen", e);
    }
  }

}
