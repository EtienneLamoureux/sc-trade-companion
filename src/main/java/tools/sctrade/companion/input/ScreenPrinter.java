package tools.sctrade.companion.input;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageProcessor;
import tools.sctrade.companion.utils.TimeFormat;
import tools.sctrade.companion.utils.TimeUtil;

public class ScreenPrinter implements Runnable {
  private final Logger logger = LoggerFactory.getLogger(ScreenPrinter.class);

  private static final String BMP = "bmp";

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

      saveToDisk(screenCapture);
    } catch (Exception e) {
      logger.error("Error while printing screen", e);
    }
  }

  private void saveToDisk(BufferedImage screenCapture) {
    String filename = TimeUtil.getNowAsString(TimeFormat.SCREENSHOT);
    File imageFile = new File(String.format(Locale.ROOT, "screenshots/%s.%s", filename, BMP));

    try {
      ImageIO.write(screenCapture, BMP, imageFile);
    } catch (IOException e) {
      logger.error("Error while saving image file to disk", e);
    }
  }
}
