package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

class ScreenshotStatusTest {

  @Test
  void givenQueuedStatus_whenReadingPresentationMetadata_thenIconTextAndStyleMatch() {
    assertEquals(MaterialDesign.MDI_CLOCK, ScreenshotStatus.QUEUED.icon());
    assertEquals("In queue", ScreenshotStatus.QUEUED.defaultText());
    assertEquals("screenshot-status-muted", ScreenshotStatus.QUEUED.styleClass());
  }

  @Test
  void givenProcessingStatus_whenReadingPresentationMetadata_thenIconTextAndStyleMatch() {
    assertEquals(MaterialDesign.MDI_CLOCK, ScreenshotStatus.PROCESSING.icon());
    assertEquals("Processing...", ScreenshotStatus.PROCESSING.defaultText());
    assertEquals("screenshot-status-normal", ScreenshotStatus.PROCESSING.styleClass());
  }

  @Test
  void givenSuccessStatus_whenReadingPresentationMetadata_thenIconTextAndStyleMatch() {
    assertEquals(MaterialDesign.MDI_CHECK_CIRCLE_OUTLINE, ScreenshotStatus.SUCCESS.icon());
    assertEquals("Success", ScreenshotStatus.SUCCESS.defaultText());
    assertEquals("screenshot-status-success", ScreenshotStatus.SUCCESS.styleClass());
  }

  @Test
  void givenErrorStatus_whenReadingPresentationMetadata_thenIconTextAndStyleMatch() {
    assertEquals(MaterialDesign.MDI_ALERT_CIRCLE_OUTLINE, ScreenshotStatus.ERROR.icon());
    assertEquals("Error", ScreenshotStatus.ERROR.defaultText());
    assertEquals("screenshot-status-danger", ScreenshotStatus.ERROR.styleClass());
  }
}
