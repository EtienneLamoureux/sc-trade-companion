package tools.sctrade.companion.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageIO;

public class ImageUtil {
  private static final String JPG = "jpg";
  private static final String DEFAULT_PATH = "screenshots";

  private ImageUtil() {}

  public static BufferedImage getFromResourcePath(String resourcePath) throws IOException {
    return ImageIO.read(ImageUtil.class.getResourceAsStream(resourcePath));
  }

  public static BufferedImage makeGreyscaleCopy(BufferedImage image) {
    BufferedImage greyscaleImage =
        new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    Graphics graphics = greyscaleImage.getGraphics();
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();

    return greyscaleImage;
  }

  public static void invertColors(BufferedImage image) {
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int rgba = image.getRGB(x, y);
        Color color = new Color(rgba, true);
        color = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
        image.setRGB(x, y, color.getRGB());
      }
    }
  }

  public static void scaleAndOffsetColors(BufferedImage image, float scale, float offset) {
    RescaleOp op = new RescaleOp(scale, offset, null);
    op.filter(image, image);
  }

  public static BufferedImage makeCopy(BufferedImage image) {
    BufferedImage clonedImage =
        new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    Graphics graphics = clonedImage.getGraphics();
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();

    return clonedImage;
  }

  public static void writeToDisk(BufferedImage screenCapture) throws IOException {
    writeToDisk(screenCapture, DEFAULT_PATH);
  }

  public static void writeToDisk(BufferedImage screenCapture, String path) throws IOException {
    String filename = TimeUtil.getNowAsString(TimeFormat.SCREENSHOT);
    File imageFile = new File(String.format(Locale.ROOT, "%s/%s.%s", path, filename, JPG));
    ImageIO.write(screenCapture, JPG, imageFile);
  }
}
