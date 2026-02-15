package tools.sctrade.companion.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opencv.core.Point;

class ImageUtilKioskTest {
  @Test
  void givenTwoPointsWhenCalculatingDistanceThenReturnCorrectValue() {
    Point p1 = new Point(0, 0);
    Point p2 = new Point(3, 4);

    double distance = ImageUtil.distance(p1, p2);

    assertEquals(5.0, distance, 0.001);
  }

  @Test
  void givenFourCornersWhenOrderingThenReturnCorrectOrder() {
    // Create corners in random order: TL, TR, BR, BL
    Point tl = new Point(10, 10); // Top-left
    Point tr = new Point(100, 20); // Top-right
    Point br = new Point(90, 80); // Bottom-right
    Point bl = new Point(15, 75); // Bottom-left

    // Pass them in random order
    Point[] corners = new Point[] {br, tl, bl, tr};

    Point[] ordered = ImageUtil.orderCorners(corners);

    // Verify order: TL, TR, BR, BL
    assertEquals(4, ordered.length);
    assertEquals(tl.x, ordered[0].x, 0.001);
    assertEquals(tl.y, ordered[0].y, 0.001);
    assertEquals(tr.x, ordered[1].x, 0.001);
    assertEquals(tr.y, ordered[1].y, 0.001);
    assertEquals(br.x, ordered[2].x, 0.001);
    assertEquals(br.y, ordered[2].y, 0.001);
    assertEquals(bl.x, ordered[3].x, 0.001);
    assertEquals(bl.y, ordered[3].y, 0.001);
  }

  @Test
  void givenWrongNumberOfCornersWhenOrderingThenThrowException() {
    Point[] corners = new Point[] {new Point(0, 0), new Point(1, 1)};

    assertThrows(IllegalArgumentException.class, () -> ImageUtil.orderCorners(corners));
  }

  @Test
  void givenIdenticalPointsWhenCalculatingDistanceThenReturnZero() {
    Point p1 = new Point(5, 5);
    Point p2 = new Point(5, 5);

    double distance = ImageUtil.distance(p1, p2);

    assertEquals(0.0, distance, 0.001);
  }
}
