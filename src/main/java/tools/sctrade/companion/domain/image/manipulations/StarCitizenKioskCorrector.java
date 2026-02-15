package tools.sctrade.companion.domain.image.manipulations;

import java.util.ArrayList;
import java.util.List;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Detects and perspective-corrects Star Citizen in-game kiosk screens by isolating the dark bezel
 * via HSV masking, selecting the largest external rectangular contour matching kiosk geometry,
 * ordering its corners, computing a homography, and warping the image to a fixed 16:9 output.
 */
public class StarCitizenKioskCorrector {
  private static final Logger logger = LoggerFactory.getLogger(StarCitizenKioskCorrector.class);
  private static final double MIN_AREA_RATIO = 0.15;
  private static final double MIN_ASPECT_RATIO = 1.4;
  private static final double MAX_ASPECT_RATIO = 1.9;
  private static final double MIN_HEIGHT_TO_WIDTH_RATIO = 0.75;
  private static final double TARGET_ASPECT_RATIO = 16.0 / 9.0;

  static {
    OpenCV.loadShared();
  }

  /**
   * Load an image from disk.
   *
   * @param path Path to the image file.
   * @return The loaded image as a Mat.
   */
  public Mat loadImage(String path) {
    Mat image = Imgcodecs.imread(path);
    if (image.empty()) {
      throw new IllegalArgumentException("Image not loaded from path: " + path);
    }
    return image;
  }

  /**
   * Preprocess the image for kiosk detection. This includes HSV masking to isolate the dark bezel,
   * followed by Gaussian blur and edge detection.
   *
   * @param input The input image.
   * @return Edge-detected image.
   */
  public Mat preprocess(Mat input) {
    Mat hsv = new Mat();
    Mat mask = new Mat();
    Mat edges = new Mat();

    // Convert to HSV and apply mask to isolate dark bezel
    Imgproc.cvtColor(input, hsv, Imgproc.COLOR_BGR2HSV);
    Core.inRange(hsv, new Scalar(0, 0, 20), new Scalar(180, 60, 120), mask);

    // Apply Gaussian blur and edge detection
    Imgproc.GaussianBlur(mask, mask, new Size(5, 5), 0);
    Imgproc.Canny(mask, edges, 50, 150);

    hsv.release();
    mask.release();

    return edges;
  }

  /**
   * Detect the kiosk contour in the edge-detected image.
   *
   * @param edges Edge-detected image.
   * @param original Original image for dimension reference.
   * @return The kiosk contour as a MatOfPoint2f with 4 corners.
   */
  public MatOfPoint2f detectKioskContour(Mat edges, Mat original) {
    List<MatOfPoint> contours = new ArrayList<>();
    Mat hierarchy = new Mat();

    // Find external contours only
    Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL,
        Imgproc.CHAIN_APPROX_SIMPLE);

    double imageArea = original.width() * original.height();
    MatOfPoint2f bestCandidate = null;
    double maxArea = 0;

    for (MatOfPoint contour : contours) {
      // Approximate polygon
      MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
      double perimeter = Imgproc.arcLength(contour2f, true);
      MatOfPoint2f approx = new MatOfPoint2f();
      Imgproc.approxPolyDP(contour2f, approx, 0.02 * perimeter, true);

      // Must have exactly 4 points
      if (approx.rows() != 4) {
        contour2f.release();
        approx.release();
        continue;
      }

      double area = Imgproc.contourArea(approx);

      // Filter by area (must be at least 15% of image)
      if (area < imageArea * MIN_AREA_RATIO) {
        contour2f.release();
        approx.release();
        continue;
      }

      // Get bounding rectangle for aspect ratio check
      org.opencv.core.Rect rect = Imgproc.boundingRect(approx);
      double aspectRatio = (double) rect.width / rect.height;

      // Filter by aspect ratio
      if (aspectRatio < MIN_ASPECT_RATIO || aspectRatio > MAX_ASPECT_RATIO) {
        contour2f.release();
        approx.release();
        continue;
      }

      // Filter by height-to-width ratio
      double heightToWidthRatio = (double) rect.height / rect.width;
      if (heightToWidthRatio < MIN_HEIGHT_TO_WIDTH_RATIO) {
        contour2f.release();
        approx.release();
        continue;
      }

      // Select the largest valid contour
      if (area > maxArea) {
        if (bestCandidate != null) {
          bestCandidate.release();
        }
        bestCandidate = approx;
        maxArea = area;
      } else {
        approx.release();
      }

      contour2f.release();
    }

    hierarchy.release();

    if (bestCandidate == null) {
      throw new RuntimeException("No valid kiosk contour detected in image");
    }

    logger.debug("Detected kiosk contour with area: {} ({}% of image)", maxArea,
        String.format("%.2f", (maxArea / imageArea) * 100));

    return bestCandidate;
  }

  /**
   * Order corners in the following order: top-left, top-right, bottom-right, bottom-left.
   *
   * @param contour The contour with 4 points.
   * @return The ordered corners.
   */
  public Point[] orderCorners(MatOfPoint2f contour) {
    Point[] points = contour.toArray();
    if (points.length != 4) {
      throw new IllegalArgumentException("Expected 4 corners, got " + points.length);
    }

    return ImageUtil.orderCorners(points);
  }

  /**
   * Warp the input image to correct perspective based on the detected corners.
   *
   * @param input The input image.
   * @param corners The ordered corners (TL, TR, BR, BL).
   * @return The perspective-corrected image.
   */
  public Mat warpPerspective(Mat input, Point[] corners) {
    if (corners.length != 4) {
      throw new IllegalArgumentException("Expected 4 corners, got " + corners.length);
    }

    Point tl = corners[0];
    Point tr = corners[1];
    Point br = corners[2];
    Point bl = corners[3];

    // Calculate output dimensions
    double widthA = ImageUtil.distance(br, bl);
    double widthB = ImageUtil.distance(tr, tl);
    int maxWidth = (int) Math.max(widthA, widthB);

    double heightA = ImageUtil.distance(tr, br);
    double heightB = ImageUtil.distance(tl, bl);
    int maxHeight = (int) Math.max(heightA, heightB);

    // Enforce target aspect ratio (16:9)
    maxHeight = (int) (maxWidth / TARGET_ASPECT_RATIO);

    // Define source and destination points
    MatOfPoint2f src = new MatOfPoint2f(tl, tr, br, bl);
    MatOfPoint2f dst = new MatOfPoint2f(new Point(0, 0), new Point(maxWidth - 1, 0),
        new Point(maxWidth - 1, maxHeight - 1), new Point(0, maxHeight - 1));

    // Compute homography and warp
    Mat homography = Imgproc.getPerspectiveTransform(src, dst);
    Mat output = new Mat();
    Imgproc.warpPerspective(input, output, homography, new Size(maxWidth, maxHeight));

    src.release();
    dst.release();
    homography.release();

    logger.debug("Warped image to dimensions: {}x{}", maxWidth, maxHeight);

    return output;
  }

  /**
   * Main method to detect and correct perspective of a Star Citizen kiosk screen.
   *
   * @param imagePath Path to the input image.
   * @return The perspective-corrected kiosk screen.
   */
  public Mat correct(String imagePath) {
    Mat input = loadImage(imagePath);
    Mat edges = preprocess(input);
    MatOfPoint2f contour = detectKioskContour(edges, input);
    Point[] corners = orderCorners(contour);
    Mat result = warpPerspective(input, corners);

    edges.release();
    contour.release();
    input.release();

    return result;
  }
}
