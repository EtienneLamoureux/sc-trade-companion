package tools.sctrade.companion.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import nu.pattern.OpenCV;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.exceptions.ImageProcessingException;
import tools.sctrade.companion.exceptions.RectangleOutOfBoundsException;

public class ImageUtil {
  private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);

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

  public static BufferedImage makeWeightedGreyscaleCopy(BufferedImage image, double coefficientRed, double coefficientGreen, double coefficientBlue) {
    BufferedImage greyscaleImage =
        new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < image.getWidth(); x++) {
          for (int y = 0; y < image.getHeight(); y++) {
            int rgba = image.getRGB(x, y);
            Color color = new Color(rgba, false);
            int red = color.getRed();
            int green = color.getGreen();
            int blue = color.getBlue();

            //Multiply each channel by it's coefficent, then add the results.
            //1,0,0 would generate an exact copy of the red channel with no contrabution of other channels
            //0,0,2 would double the values of the blue channel and use that
            //Note that no averaging is done in this algorithm
            double accumulate = (coefficientRed * red) + (coefficientBlue * blue) + (coefficientGreen * green);

            //clip values to integers of 0 - 255
            int greyValue = 0;
            if(accumulate>255){
              greyValue=255;
            }
            else{
              if(accumulate<0){
                greyValue=0;
              }
              else{
                //truncate the decimal in the accumulator
                greyValue = (int)accumulate;
              }
            }

            greyscaleImage.setRGB(x, y, new Color(greyValue, greyValue, greyValue).getRGB());        
          }
        }
    return greyscaleImage;
  }

  public static void invertColors(BufferedImage image) {
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int rgba = image.getRGB(x, y);
        Color color = new Color(rgba, false);
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

  public static Color calculateDominantColor(BufferedImage image) {
    return calculateDominantColor(image, new Rectangle(0, 0, image.getWidth(), image.getHeight()));
  }

  public static Color calculateDominantColor(BufferedImage image, Rectangle rectangle) {
    Rectangle imageRectangle =
        new Rectangle(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight());

    if (!imageRectangle.contains(rectangle)) {
      throw new RectangleOutOfBoundsException(rectangle, imageRectangle);
    }

    var countByApproximateColors = new HashMap<Color, Integer>();

    for (int x = (int) rectangle.getMinX(); x < rectangle.getMaxX(); x++) {
      for (int y = (int) rectangle.getMinY(); y < rectangle.getMaxY(); y++) {
        Color pixel = new Color(image.getRGB(x, y));
        int approximateRed = pixel.getRed() - (pixel.getRed() % 10);
        int approximateGreen = pixel.getGreen() - (pixel.getGreen() % 10);
        int approximateBlue = pixel.getBlue() - (pixel.getBlue() % 10);
        Color approximateColor = new Color(approximateRed, approximateGreen, approximateBlue);
        countByApproximateColors.put(approximateColor,
            countByApproximateColors.getOrDefault(approximateColor, 0) + 1);
      }
    }

    var approximateColorsByCount = new HashMap<Integer, Color>();

    for (Entry<Color, Integer> entry : countByApproximateColors.entrySet()) {
      approximateColorsByCount.put(entry.getValue(), entry.getKey());
    }

    return approximateColorsByCount.get(Collections.max(approximateColorsByCount.keySet()));
  }

  public static Color calculateAverageColor(BufferedImage image) {
    return calculateAverageColor(image, new Rectangle(0, 0, image.getWidth(), image.getHeight()));
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

  public static BufferedImage crop(BufferedImage image, Rectangle rectangle) {
    BufferedImage croppedImage = image.getSubimage((int) rectangle.getMinX(),
        (int) rectangle.getMinY(), (int) rectangle.getWidth(), (int) rectangle.getHeight());

    return makeCopy(croppedImage);
  }

  public static BufferedImage applyAdaptiveGaussianThreshold(BufferedImage image,
      int pixelNeighborhoodSize, int substractedConstant) {
    OpenCV.loadShared();

    try {
      Mat original = toMat(image);
      Mat processed = new Mat(original.rows(), original.cols(), original.type());

      Imgproc.adaptiveThreshold(original, processed, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
          Imgproc.THRESH_BINARY, pixelNeighborhoodSize, substractedConstant);

      return toBufferedImage(processed);
    } catch (IOException e) {
      throw new ImageProcessingException(e);
    }
  }

  public static BufferedImage applyOtsuBinarization(BufferedImage image) {
    OpenCV.loadShared();

    try {
      Mat original = toMat(image);
      Mat filtered = new Mat(original.rows(), original.cols(), original.type());
      Mat processed = new Mat(original.rows(), original.cols(), original.type());

      Imgproc.GaussianBlur(original, filtered, new Size(5, 5), 0);
      Imgproc.threshold(filtered, processed, Imgproc.THRESH_BINARY, 255, Imgproc.THRESH_OTSU);

      return toBufferedImage(processed);
    } catch (IOException e) {
      throw new ImageProcessingException(e);
    }
  }

  public static BufferedImage applyThreshBinarization(BufferedImage image, int threshold) {
    OpenCV.loadShared();

    try {
      Mat original = toMat(image);
      Mat filtered = new Mat(original.rows(), original.cols(), original.type());
      Mat processed = new Mat(original.rows(), original.cols(), original.type());

      Imgproc.threshold(filtered, processed, threshold, 255, Imgproc.THRESH_BINARY);

      return toBufferedImage(processed);
    } catch (IOException e) {
      throw new ImageProcessingException(e);
    }
  }

  public static BufferedImage makeDilatedImage(BufferedImage image) {
    OpenCV.loadShared();

    //Dilate the font twice using a 2,2 'cross' kernel
    try {
      Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2, 2));

      Mat original = toMat(image);

      Mat dilatedImage = new Mat();
      Imgproc.dilate(original, dilatedImage, kernel);
    
      Mat dilatedImage2 = new Mat();
      Imgproc.dilate(dilatedImage, dilatedImage2, kernel);
    
      return toBufferedImage(dilatedImage2);
    } catch (IOException e) {
      throw new ImageProcessingException(e);
    }
  }



  public static List<Rectangle> findBoundingBoxes(BufferedImage image) {
    OpenCV.loadShared();

    try {
      Mat original = toMat(image);

      List<MatOfPoint> contours = new ArrayList<>();
      Imgproc.findContours(original, contours, new Mat(), Imgproc.RETR_TREE,
          Imgproc.CHAIN_APPROX_SIMPLE);

      return contours.parallelStream().map(n -> Imgproc.boundingRect(n))
          .map(n -> new Rectangle(n.x, n.y, n.width, n.height)).collect(Collectors.toList());
    } catch (IOException e) {
      throw new ImageProcessingException(e);
    }
  }

  /**
   * @see https://answers.opencv.org/question/28348/converting-bufferedimage-to-mat-in-java/
   */
  public static Mat toMat(BufferedImage image) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", byteArrayOutputStream);
    byteArrayOutputStream.flush();

    return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()),
        Imgcodecs.IMREAD_ANYCOLOR);
  }

  public static BufferedImage toBufferedImage(Mat mat) throws IOException {
    MatOfByte mob = new MatOfByte();
    Imgcodecs.imencode(".jpg", mat, mob);

    return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
  }

  public static void writeToDiskNoFail(BufferedImage image, Path path) {
    try {
      writeToDisk(image, path.toAbsolutePath());
    } catch (Exception e) {
      logger.error("There was an error writing to disk", e);
    }
  }

  static void writeToDisk(BufferedImage image, Path path) throws IOException {
    Files.createDirectories(path.getParent());
    File imageFile = new File(path.toString());
    String format = path.toString().substring(path.toString().lastIndexOf(".") + 1);
    ImageIO.write(image, format, imageFile);
  }
}
