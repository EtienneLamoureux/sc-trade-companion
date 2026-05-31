package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ScreenshotTest {

  private static final Screenshot BASE = new Screenshot("id-1", null, "Orison",
      ScreenshotStatus.QUEUED, null, null, ScreenshotType.COMMODITY_KIOSK);

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
