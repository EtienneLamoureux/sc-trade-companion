package tools.sctrade.companion.input;

import java.awt.AWTException;
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
import tools.sctrade.companion.domain.image.ImageProcessor;

public class ScreenPrinter implements Runnable {
  private Collection<ImageProcessor> imageProcessors;

  public ScreenPrinter(Collection<ImageProcessor> imageProcessors) {
    this.imageProcessors = imageProcessors; // TODO
  }

  @Override
  public void run() {
    try {
      Rectangle screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
      BufferedImage screenCapture = new Robot().createScreenCapture(screenRectangle);

      File imageFile = new File(
          String.format(Locale.ROOT, "screenshots/%s.bmp", String.valueOf(new Random().nextInt())));
      ImageIO.write(screenCapture, "bmp", imageFile);
    } catch (IOException | AWTException e) {
      e.printStackTrace();
    }
  }
}
