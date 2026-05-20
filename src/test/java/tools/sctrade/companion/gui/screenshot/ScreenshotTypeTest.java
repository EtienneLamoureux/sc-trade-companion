package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScreenshotTypeTest {

  @Test
  void givenCommodityKioskType_whenReadingLabel_thenReturnsExpectedLabel() {
    assertEquals("Commodity kiosk", ScreenshotType.COMMODITY_KIOSK.label());
  }

  @Test
  void givenItemKioskType_whenReadingLabel_thenReturnsExpectedLabel() {
    assertEquals("Item kiosk", ScreenshotType.ITEM_KIOSK.label());
  }
}
