package tools.sctrade.companion.domain.image.manipulations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

class StarCitizenKioskCorrectorTest {
  private StarCitizenKioskCorrector corrector;

  @BeforeEach
  void setUp() {
    corrector = new StarCitizenKioskCorrector();
  }

  @Test
  void givenInvalidPathWhenLoadingImageThenThrowException() {
    assertThrows(IllegalArgumentException.class,
        () -> corrector.loadImage("/nonexistent/path/image.jpg"));
  }

  @Test
  void givenValidMatWhenPreprocessingThenReturnEdges() {
    // Create a simple test image (3x3 BGR image)
    Mat testImage = new Mat(100, 100, org.opencv.core.CvType.CV_8UC3);

    Mat edges = corrector.preprocess(testImage);

    assertNotNull(edges);
    assertEquals(100, edges.rows());
    assertEquals(100, edges.cols());

    edges.release();
    testImage.release();
  }

  @Test
  void givenFourPointsWhenOrderingCornersThenReturnCorrectOrder() {
    // Create corners in random order
    Point tl = new Point(10, 10);
    Point tr = new Point(100, 20);
    Point br = new Point(90, 80);
    Point bl = new Point(15, 75);

    MatOfPoint2f contour = new MatOfPoint2f(br, tl, bl, tr);

    Point[] ordered = corrector.orderCorners(contour);

    assertEquals(4, ordered.length);
    // Verify top-left is first
    assertEquals(tl.x, ordered[0].x, 0.001);
    assertEquals(tl.y, ordered[0].y, 0.001);

    contour.release();
  }

  @Test
  void givenWrongNumberOfPointsWhenOrderingCornersThenThrowException() {
    MatOfPoint2f contour = new MatOfPoint2f(new Point(0, 0), new Point(1, 1));

    assertThrows(IllegalArgumentException.class, () -> corrector.orderCorners(contour));

    contour.release();
  }

  @Test
  void givenValidCornersWhenWarpingPerspectiveThenReturnWarpedImage() {
    Mat testImage = new Mat(200, 300, org.opencv.core.CvType.CV_8UC3);

    // Create valid corners for perspective transform
    Point[] corners = new Point[] {new Point(50, 50), new Point(250, 50), new Point(250, 150),
        new Point(50, 150)};

    Mat warped = corrector.warpPerspective(testImage, corners);

    assertNotNull(warped);
    // Verify output dimensions (16:9 aspect ratio enforced)
    assertEquals((int) (warped.width() / 16.0 * 9), warped.height());

    warped.release();
    testImage.release();
  }

  @Test
  void givenWrongNumberOfCornersWhenWarpingPerspectiveThenThrowException() {
    Mat testImage = new Mat(100, 100, org.opencv.core.CvType.CV_8UC3);
    Point[] corners = new Point[] {new Point(0, 0), new Point(1, 1)};

    assertThrows(IllegalArgumentException.class,
        () -> corrector.warpPerspective(testImage, corners));

    testImage.release();
  }
}
