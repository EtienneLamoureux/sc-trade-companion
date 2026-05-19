package tools.sctrade.companion.domain.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class ScreenshotTest {

  @Test
  void whenMutatingOriginalImageAfterConstruction_thenStoredImageIsUnchanged() {
    BufferedImage original = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    original.setRGB(0, 0, 0xFF0000); // red pixel

    Screenshot screenshot = new Screenshot("id-1", original, null, ScreenshotStatus.QUEUED, null,
        null, ScreenshotType.COMMODITY_KIOSK);

    original.setRGB(0, 0, 0x0000FF); // mutate to blue

    assertEquals(0xFF0000, screenshot.image().getRGB(0, 0) & 0xFFFFFF);
  }
}
