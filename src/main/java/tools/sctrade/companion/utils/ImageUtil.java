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

/**
 * Utility class for image manipulation.
 */
public class ImageUtil {
  private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);

  private ImageUtil() {}

  /**
   * Makes a copy of the input, turning it to greyscale with equalized color.
   *
   * @see {@link org.opencv.imgproc.Imgproc#equalizeHist Equalized histogram}
   * @param image The image to make an equalized copy of.
   * @return copy of the input, in equalized greyscale
   */
  public static BufferedImage makeHistogramEqualizedGreyscaleCopy(BufferedImage image) {
    image = makeGreyscaleCopy(image);
    OpenCV.loadShared();

    try {
      Mat original = toMat(image);
      Mat processed = new Mat(original.rows(), original.cols(), original.type());
      Imgproc.equalizeHist(original, processed);

      return toBufferedImage(processed);
    } catch (IOException e) {
      throw new ImageProcessingException(e);
    }
  }

  /**
   * Makes a copy of the input, turning it to greyscale with equalized color.
   *
   * @see {@link org.opencv.imgproc.CLAHE#apply Contrast Limited Adaptive Histogram Equalization}
   * @param image The image to make an equalized copy of.
   * @return copy of the input, in equalized greyscale
   */
  public static BufferedImage makeClaheEqualizedGreyscaleCopy(BufferedImage image) {
    image = makeGreyscaleCopy(image);
    OpenCV.loadShared();

    try {
      Mat original = toMat(image);
      Mat processed = new Mat(original.rows(), original.cols(), original.type());

      var clahe = Imgproc.createCLAHE();
      clahe.setClipLimit(3.0);
      clahe.apply(original, processed);

      return toBufferedImage(processed);
    } catch (IOException e) {
      throw new ImageProcessingException(e);
    }
  }

  /**
   * Creates a greyscale copy of an image. The original is untouched.
   *
   * @param image The image to make a greyscale copy of.
   * @return A greyscale copy of the image
   */
  public static BufferedImage makeGreyscaleCopy(BufferedImage image) {
    BufferedImage greyscaleImage =
        new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    Graphics graphics = greyscaleImage.getGraphics();
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();

    return greyscaleImage;
  }

  /**
   * Inverts the colors of an image. Inplace: transacts on the original image.
   *
   * @param image The image to invert the colors of.
   */
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

  /**
   * Adjusts the brightness and contrast of an image. Inplace: transacts on the original
   *
   * @param image The image to adjust the brightness and contrast of.
   * @param contrastScale The contrast
   * @param brightnessOffset The brightness
   */
  public static void adjustBrightnessAndContrast(BufferedImage image, float contrastScale,
      float brightnessOffset) {
    RescaleOp op = new RescaleOp(contrastScale, brightnessOffset, null);
    op.filter(image, image);
  }

  /**
   * Calculate the most frequent color of an image. Approximate colors are used, and the most
   * frequent one is returned.
   *
   * @param image The image to calculate the dominant color of.
   * @return The dominant color of the image.
   */
  public static Color calculateDominantColor(BufferedImage image) {
    return calculateDominantColor(image, new Rectangle(0, 0, image.getWidth(), image.getHeight()));
  }

  /**
   * Calculate the most frequent color of a rectangle in an image. Approximate colors are used, and
   * the most frequent one is returned.
   *
   * @param image The source image.
   * @param rectangle The rectangle to calculate the dominant color of, inside the image.
   * @return The dominant color of the rectangle.
   */
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

  /**
   * Calculate the average color of an image. As in, if all the pixels were mixed together, what
   * color would the result be?
   *
   * @param image The image to calculate the average color of.
   * @return The average color of the image.
   */
  public static Color calculateAverageColor(BufferedImage image) {
    return calculateAverageColor(image, new Rectangle(0, 0, image.getWidth(), image.getHeight()));
  }

  /**
   * Calculate the average color of a rectangle in an image. As in, if all the pixels were mixed
   * together, what color would the result be?
   *
   * @param image The source image.
   * @param rectangle The rectangle to calculate the average color of, inside the image.
   * @return The average color of the rectangle.
   */
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

  /**
   * Scale an image to a specific height, maintaining aspect ratio.
   *
   * @param image The image to scale.
   * @param height The height to scale the image to.
   * @return The scaled image.
   */
  public static BufferedImage scaleToHeight(BufferedImage image, int height) {
    return Scalr.resize(image, Method.ULTRA_QUALITY, Mode.FIT_TO_HEIGHT, height, height);
  }

  /**
   * Creates a new BufferedImage with the same content as the original.
   *
   * @param image The original image.
   * @return A copy of the original image.
   */
  public static BufferedImage makeCopy(BufferedImage image) {
    BufferedImage clonedImage =
        new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    Graphics graphics = clonedImage.getGraphics();
    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();

    return clonedImage;
  }

  /**
   * Crop an image to a specific rectangle.
   *
   * @param image The image to crop.
   * @param rectangle The rectangle to crop the image to.
   * @return The cropped image.
   */
  public static BufferedImage crop(BufferedImage image, Rectangle rectangle) {
    BufferedImage croppedImage = image.getSubimage((int) rectangle.getMinX(),
        (int) rectangle.getMinY(), (int) rectangle.getWidth(), (int) rectangle.getHeight());

    return makeCopy(croppedImage);
  }

  /**
   * Apply a Gaussian blur to an image.
   *
   * @param image The image to blur.
   * @param pixelNeighborhoodSize The size of the pixel neighborhood.
   * @param substractedConstant The constant to subtract from the mean.
   * @return The blurred image.
   */
  public static BufferedImage applyGaussianBlur(BufferedImage image, int pixelNeighborhoodSize,
      int substractedConstant) {
    OpenCV.loadShared();

    try {
      Mat original = toMat(image);
      Mat processed = new Mat(original.rows(), original.cols(), original.type());

      Imgproc.GaussianBlur(original, processed,
          new Size(pixelNeighborhoodSize, pixelNeighborhoodSize), substractedConstant);

      return toBufferedImage(processed);
    } catch (IOException e) {
      throw new ImageProcessingException(e);
    }
  }

  /**
   * Apply an adaptive Gaussian threshold to an image.
   *
   * @param image The image to apply the threshold to.
   * @param pixelNeighborhoodSize The size of the pixel neighborhood.
   * @param substractedConstant The constant to subtract from the mean.
   * @return The thresholded image.
   * @see <a href=
   *      "https://docs.opencv.org/4.x/d7/d4d/tutorial_py_thresholding.html#autotoc_md1425">Documentation</a>
   */
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

  /**
   * Apply Otsu binarization to an image.
   *
   * @param image The image to apply the binarization to.
   * @return The binarized image.
   * @see <a href=
   *      "https://docs.opencv.org/4.x/d7/d4d/tutorial_py_thresholding.html#autotoc_md1426">Documentation</a>
   */
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

  /**
   * Find bounding boxes in an image.
   *
   * @param image The image to find bounding boxes in.
   * @return A list of bounding boxes.
   * @see <a href=
   *      "https://docs.opencv.org/4.x/dd/d49/tutorial_py_contour_features.html#autotoc_md1308">Documentation</a>
   */
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
   * Convert a BufferedImage to an OpenCV {@link Mat}.
   *
   * @param image The image to convert.
   * 
   * @see <a href=
   *      "https://answers.opencv.org/question/28348/converting-bufferedimage-to-mat-in-java/">Source</a>
   */
  public static Mat toMat(BufferedImage image) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", byteArrayOutputStream);
    byteArrayOutputStream.flush();

    return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()),
        Imgcodecs.IMREAD_ANYCOLOR);
  }

  /**
   * Convert an OpenCV {@link Mat} to a BufferedImage.
   *
   * @param mat The mat to convert.
   * @return The converted image.
   * @throws IOException If the image could not be processed.
   */
  public static BufferedImage toBufferedImage(Mat mat) throws IOException {
    MatOfByte mob = new MatOfByte();
    Imgcodecs.imencode(".jpg", mat, mob);

    return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
  }

  /**
   * Writes an image to disk, logging any exceptions that occur. Never throws an exception.
   *
   * @param image The image to write
   * @param path The path to write the image to
   */
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
