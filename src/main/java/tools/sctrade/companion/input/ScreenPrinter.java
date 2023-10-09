package tools.sctrade.companion.input;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Random;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageProcessor;

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

      saveToDisk(screenCapture);
    } catch (Exception e) {
      logger.error("Error while printing screen", e);
    }
  }

  private void saveToDisk(BufferedImage screenCapture) {
    String filename = String.valueOf(new Random().nextInt());
    File imageFile = new File(String.format(Locale.ROOT, "screenshots/%s.bmp", filename));

    try {
      ImageIO.write(screenCapture, "bmp", imageFile);
    } catch (IOException e) {
      logger.error("Error while saving image file to disk", e);
    }
  }
}
