package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScreenshotStatusTest {

  @Test
  void givenQueuedStatus_whenReadingPresentationMetadata_thenIconTextAndStyleMatch() {
    assertEquals("mdoal-access_time", ScreenshotStatus.QUEUED.iconClass());
    assertEquals("In queue", ScreenshotStatus.QUEUED.defaultText());
    assertEquals("screenshot-status-muted", ScreenshotStatus.QUEUED.styleClass());
  }

  @Test
  void givenProcessingStatus_whenReadingPresentationMetadata_thenIconTextAndStyleMatch() {
    assertEquals("mdoal-access_time", ScreenshotStatus.PROCESSING.iconClass());
    assertEquals("Processing...", ScreenshotStatus.PROCESSING.defaultText());
    assertEquals("screenshot-status-normal", ScreenshotStatus.PROCESSING.styleClass());
  }

  @Test
  void givenSuccessStatus_whenReadingPresentationMetadata_thenIconTextAndStyleMatch() {
    assertEquals("mdal-check_circle_outline", ScreenshotStatus.SUCCESS.iconClass());
    assertEquals("Success", ScreenshotStatus.SUCCESS.defaultText());
    assertEquals("screenshot-status-success", ScreenshotStatus.SUCCESS.styleClass());
  }

  @Test
  void givenErrorStatus_whenReadingPresentationMetadata_thenIconTextAndStyleMatch() {
    assertEquals("mdsal-error_outline", ScreenshotStatus.ERROR.iconClass());
    assertEquals("Error", ScreenshotStatus.ERROR.defaultText());
    assertEquals("screenshot-status-danger", ScreenshotStatus.ERROR.styleClass());
  }
}
