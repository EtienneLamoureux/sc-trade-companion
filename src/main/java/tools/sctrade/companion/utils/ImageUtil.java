package tools.sctrade.companion.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import tools.sctrade.companion.exceptions.RectangleOutOfBoundsException;

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

  public static void adjustBrightnessAndContrast(BufferedImage image, float contrastScale,
      float brightnessOffset) {
    RescaleOp op = new RescaleOp(contrastScale, brightnessOffset, null);
    op.filter(image, image);
  }

  public static Color calculateAverageColor(BufferedImage image, Rectangle rectangle) {
    Rectangle imageRectangle =
        new Rectangle(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight());

    if (!imageRectangle.contains(rectangle)) {
      throw new RectangleOutOfBoundsException(rectangle, imageRectangle);
    }

    int totalRed = 0;
    int totalGreen = 0;
    int totalBlue = 0;

    for (int x = (int) rectangle.getMinX(); x < rectangle.getMaxX(); x++) {
      for (int y = (int) rectangle.getMinY(); y < rectangle.getMaxY(); y++) {
        Color pixel = new Color(image.getRGB(x, y));
        totalRed += pixel.getRed();
        totalGreen += pixel.getGreen();
        totalBlue += pixel.getBlue();
      }
    }

    int pixelCount = (int) (rectangle.getWidth() * rectangle.getHeight());
    int averageRed = totalRed / pixelCount;
    int averageGreen = totalGreen / pixelCount;
    int averageBlue = totalBlue / pixelCount;

    return new Color(averageRed, averageGreen, averageBlue);
  }

  public static BufferedImage scaleToHeight(BufferedImage image, int height) {
    return Scalr.resize(image, Method.ULTRA_QUALITY, Mode.FIT_TO_HEIGHT, height, height);
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
    String filename = TimeUtil.getNowAsString(TimeFormat.SCREENSHOT_FILENAME);
    File imageFile = new File(String.format(Locale.ROOT, "%s/%s.%s", path, filename, JPG));
    ImageIO.write(screenCapture, JPG, imageFile);
  }
}
