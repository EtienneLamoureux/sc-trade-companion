package tools.sctrade.companion.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.utils.ImageUtil.PerspectiveCorrectionResult;

class ImageUtilPerspectiveTest {
  @Test
  void givenSimpleImageWhenApplyingPerspectiveCorrectionThenReturnResult() {
    // Create a simple test image
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, 100, 100);
    g2d.setColor(Color.BLACK);
    g2d.drawRect(10, 10, 80, 80);
    g2d.dispose();

    PerspectiveCorrectionResult result = ImageUtil.applyPerspectiveCorrection(image);

    assertNotNull(result);
    assertNotNull(result.image());
    assertTrue(result.image().getWidth() > 0);
    assertTrue(result.image().getHeight() > 0);
  }

  @Test
  void givenImageWithoutQuadrilateralWhenApplyingPerspectiveCorrectionThenReturnOriginalWithNullRect() {
    // Create a simple image without clear quadrilateral
    BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, 50, 50);
    g2d.dispose();

    PerspectiveCorrectionResult result = ImageUtil.applyPerspectiveCorrection(image);

    assertNotNull(result);
    assertNotNull(result.image());
    assertNull(result.rectangle());
    assertEquals(50, result.image().getWidth());
    assertEquals(50, result.image().getHeight());
  }

  @Test
  void givenImageWhenCroppingToLargestRectangleThenReturnCroppedImage() {
    // Create an image with a black border and white content
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(Color.BLACK);
    g2d.fillRect(0, 0, 100, 100);
    g2d.setColor(Color.WHITE);
    g2d.fillRect(10, 10, 80, 80);
    g2d.dispose();

    BufferedImage result = ImageUtil.cropToLargestRectangle(image);

    assertNotNull(result);
    assertTrue(result.getWidth() > 0);
    assertTrue(result.getHeight() > 0);
    // The cropped image should be smaller than or equal to the original
    assertTrue(result.getWidth() <= image.getWidth());
    assertTrue(result.getHeight() <= image.getHeight());
  }

  @Test
  void givenUniformImageWhenCroppingToLargestRectangleThenReturnSameSize() {
    // Create a uniform white image
    BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, 50, 50);
    g2d.dispose();

    BufferedImage result = ImageUtil.cropToLargestRectangle(image);

    assertNotNull(result);
    assertEquals(50, result.getWidth());
    assertEquals(50, result.getHeight());
  }
}
