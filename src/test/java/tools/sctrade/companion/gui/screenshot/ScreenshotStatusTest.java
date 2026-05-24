package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScreenshotStatusTest {

  @Test
  void givenQueuedStatus_whenReadingPresentationMetadata_thenGlyphAndStyleMatch() {
    assertEquals("⏳", ScreenshotStatus.QUEUED.glyph());
    assertEquals("In queue", ScreenshotStatus.QUEUED.defaultText());
    assertEquals("screenshot-status-muted", ScreenshotStatus.QUEUED.styleClass());
  }

  @Test
  void givenProcessingStatus_whenReadingPresentationMetadata_thenGlyphAndStyleMatch() {
    assertEquals("⚙", ScreenshotStatus.PROCESSING.glyph());
    assertEquals("Processing...", ScreenshotStatus.PROCESSING.defaultText());
    assertEquals("screenshot-status-normal", ScreenshotStatus.PROCESSING.styleClass());
  }

  @Test
  void givenSuccessStatus_whenReadingPresentationMetadata_thenGlyphAndStyleMatch() {
    assertEquals("✅", ScreenshotStatus.SUCCESS.glyph());
    assertEquals("Success", ScreenshotStatus.SUCCESS.defaultText());
    assertEquals("screenshot-status-success", ScreenshotStatus.SUCCESS.styleClass());
  }

  @Test
  void givenErrorStatus_whenReadingPresentationMetadata_thenGlyphAndStyleMatch() {
    assertEquals("⚠", ScreenshotStatus.ERROR.glyph());
    assertEquals("Error", ScreenshotStatus.ERROR.defaultText());
    assertEquals("screenshot-status-danger", ScreenshotStatus.ERROR.styleClass());
  }
}
