package tools.sctrade.companion.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.image.manipulations.LargestRectangleCrop;
import tools.sctrade.companion.domain.image.manipulations.PerspectiveCorrection;

/**
 * Demo test to visualize the perspective correction and largest rectangle crop functionality. This
 * test is disabled by default as it's for demonstration purposes only.
 */
@Disabled("Demo test - enable manually to generate sample images")
class PerspectiveCorrectionDemoTest {

  @Test
  void demonstratePerspectiveCorrectionPipeline() throws IOException {
    // Create a test image that simulates a skewed screen with a border
    BufferedImage testImage = createTestImage();

    // Save original
    ImageIO.write(testImage, "png", new File("/tmp/demo_original.png"));

    // Apply perspective correction
    PerspectiveCorrection perspectiveCorrection = new PerspectiveCorrection();
    BufferedImage corrected = perspectiveCorrection.manipulate(testImage);
    ImageIO.write(corrected, "png", new File("/tmp/demo_corrected.png"));

    // Apply largest rectangle crop
    LargestRectangleCrop crop = new LargestRectangleCrop();
    BufferedImage cropped = crop.manipulate(corrected);
    ImageIO.write(cropped, "png", new File("/tmp/demo_final.png"));

    System.out.println("Demo images saved to /tmp/");
    System.out.println("  - demo_original.png: Original skewed image");
    System.out.println("  - demo_corrected.png: After perspective correction");
    System.out.println("  - demo_final.png: After cropping to largest rectangle");
  }

  private BufferedImage createTestImage() {
    // Create a 500x500 image with a white background
    BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();

    // Fill with black background
    g2d.setColor(Color.BLACK);
    g2d.fillRect(0, 0, 500, 500);

    // Draw a white "screen" with some content (simulating a commodity kiosk)
    g2d.setColor(Color.WHITE);
    g2d.fillRect(50, 50, 400, 400);

    // Add some "text" elements to simulate kiosk content
    g2d.setColor(Color.BLACK);
    g2d.fillRect(80, 80, 340, 30); // Header bar
    g2d.setColor(Color.GRAY);
    g2d.fillRect(80, 120, 150, 20); // Text line 1
    g2d.fillRect(80, 150, 200, 20); // Text line 2
    g2d.fillRect(80, 180, 180, 20); // Text line 3

    // Add a colored accent (simulating UI elements)
    g2d.setColor(new Color(0, 100, 200));
    g2d.fillRect(250, 120, 150, 80);

    g2d.dispose();
    return image;
  }
}
