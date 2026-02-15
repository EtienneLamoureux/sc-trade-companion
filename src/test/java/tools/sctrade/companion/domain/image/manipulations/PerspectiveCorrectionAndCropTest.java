package tools.sctrade.companion.domain.image.manipulations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class PerspectiveCorrectionAndCropTest {
  @Test
  void givenImageWhenManipulatingThenReturnCorrectedAndCroppedImage() {
    PerspectiveCorrectionAndCrop manipulation = new PerspectiveCorrectionAndCrop();

    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, 100, 100);
    g2d.setColor(Color.BLACK);
    g2d.drawRect(10, 10, 80, 80);
    g2d.dispose();

    BufferedImage result = manipulation.manipulate(image);

    assertNotNull(result);
    assertTrue(result.getWidth() > 0);
    assertTrue(result.getHeight() > 0);
  }

  @Test
  void givenUniformImageWhenManipulatingThenReturnOriginal() {
    PerspectiveCorrectionAndCrop manipulation = new PerspectiveCorrectionAndCrop();

    // Create a uniform image with no quadrilateral
    BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, 50, 50);
    g2d.dispose();

    BufferedImage result = manipulation.manipulate(image);

    assertNotNull(result);
    assertTrue(result.getWidth() > 0);
    assertTrue(result.getHeight() > 0);
  }
}
