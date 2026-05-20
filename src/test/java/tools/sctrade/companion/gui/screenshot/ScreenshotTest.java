package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class ScreenshotTest {

  private static final Screenshot BASE = new Screenshot("id-1", null, "Orison",
      ScreenshotStatus.QUEUED, null, null, ScreenshotType.COMMODITY_KIOSK);

  @Test
  void whenMutatingOriginalImageAfterConstruction_thenStoredImageIsUnchanged() {
    BufferedImage original = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    original.setRGB(0, 0, 0xFF0000); // red pixel

    Screenshot screenshot = new Screenshot("id-1", original, null, ScreenshotStatus.QUEUED, null,
        null, ScreenshotType.COMMODITY_KIOSK);

    original.setRGB(0, 0, 0x0000FF); // mutate to blue

    assertEquals(0xFF0000, screenshot.image().getRGB(0, 0) & 0xFFFFFF);
  }

  @Test
  void givenUpdateWithNonNullField_whenUpdateUsing_thenFieldIsReplaced() {
    var update = new Screenshot("id-1", null, null, ScreenshotStatus.PROCESSING, null, null, null);

    Screenshot result = BASE.updateUsing(update);

    assertEquals(ScreenshotStatus.PROCESSING, result.status());
  }

  @Test
  void givenUpdateWithNullField_whenUpdateUsing_thenOriginalFieldIsKept() {
    var update = new Screenshot("id-1", null, null, ScreenshotStatus.PROCESSING, null, null, null);

    Screenshot result = BASE.updateUsing(update);

    assertEquals("Orison", result.location());
  }

  @Test
  void givenUpdateWithAllNullOptionalFields_whenUpdateUsing_thenAllOriginalFieldsAreKept() {
    var update = new Screenshot("id-1", null, null, null, null, null, null);

    Screenshot result = BASE.updateUsing(update);

    assertEquals(BASE.location(), result.location());
    assertEquals(BASE.status(), result.status());
    assertEquals(BASE.type(), result.type());
    assertNull(result.error());
    assertNull(result.content());
  }

  @Test
  void givenUpdateWithDifferentId_whenUpdateUsing_thenThrows() {
    var update = new Screenshot("id-2", null, null, ScreenshotStatus.SUCCESS, null, null, null);

    assertThrows(IllegalArgumentException.class, () -> BASE.updateUsing(update));
  }
}
