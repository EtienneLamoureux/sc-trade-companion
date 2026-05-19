package tools.sctrade.companion.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.screenshot.ScreenshotRepository;
import tools.sctrade.companion.domain.screenshot.ScreenshotStatus;
import tools.sctrade.companion.domain.screenshot.ScreenshotType;

class StatusTrackingSubmissionFactoryTest {

  private static final ScreenshotType TYPE = ScreenshotType.COMMODITY_KIOSK;

  private ScreenshotRepository screenshotRepository;
  private BufferedImage image;

  @BeforeEach
  void setUp() {
    screenshotRepository = new ScreenshotRepository();
    image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
  }

  @Test
  void whenBuildSucceeds_thenRepositoryHasSuccessStatus() {
    var factory = new SucceedingTestFactory(screenshotRepository, "location", "content");

    factory.build(image);

    assertEquals(ScreenshotStatus.SUCCESS, screenshotRepository.getSnapshot().get(0).status());
  }

  @Test
  void whenBuildFails_thenRepositoryHasErrorStatus() {
    var factory = new FailingTestFactory(screenshotRepository, "error message");

    try {
      factory.build(image);
    } catch (RuntimeException e) {
      // expected
    }

    assertEquals(ScreenshotStatus.ERROR, screenshotRepository.getSnapshot().get(0).status());
  }

  @Test
  void whenBuildFails_thenExceptionIsRethrown() {
    var factory = new FailingTestFactory(screenshotRepository, "error message");

    assertThrows(RuntimeException.class, () -> factory.build(image));
  }

  @Test
  void whenBuildFails_thenErrorMessageIsStoredInRepository() {
    var factory = new FailingTestFactory(screenshotRepository, "error message");

    try {
      factory.build(image);
    } catch (RuntimeException e) {
      // expected
    }

    assertEquals("error message", screenshotRepository.getSnapshot().get(0).error());
  }

  @Test
  void whenBuildSucceeds_thenLocationIsStoredInRepository() {
    var factory = new SucceedingTestFactory(screenshotRepository, "test-location", "test-content");

    factory.build(image);

    assertEquals("test-location", screenshotRepository.getSnapshot().get(0).location());
  }

  @Test
  void whenBuildSucceeds_thenContentIsStoredInRepository() {
    var factory = new SucceedingTestFactory(screenshotRepository, "test-location", "test-content");

    factory.build(image);

    assertEquals("test-content", screenshotRepository.getSnapshot().get(0).content());
  }

  @Test
  void whenBuildCalled_thenScreenshotTypeIsStoredInRepository() {
    var factory = new SucceedingTestFactory(screenshotRepository, null, null);

    factory.build(image);

    assertEquals(TYPE, screenshotRepository.getSnapshot().get(0).type());
  }

  private static class SucceedingTestFactory extends StatusTrackingSubmissionFactory<String> {

    private final String location;
    private final String content;

    SucceedingTestFactory(ScreenshotRepository repo, String location, String content) {
      super(repo, ScreenshotType.COMMODITY_KIOSK);
      this.location = location;
      this.content = content;
    }

    @Override
    protected String doBuild(BufferedImage screenCapture) {
      return "result";
    }

    @Override
    protected String extractLocation(String result) {
      return location;
    }

    @Override
    protected String extractContent(String result) {
      return content;
    }
  }

  private static class FailingTestFactory extends StatusTrackingSubmissionFactory<String> {

    private final String errorMessage;

    FailingTestFactory(ScreenshotRepository repo, String errorMessage) {
      super(repo, ScreenshotType.COMMODITY_KIOSK);
      this.errorMessage = errorMessage;
    }

    @Override
    protected String doBuild(BufferedImage screenCapture) {
      throw new RuntimeException(errorMessage);
    }

    @Override
    protected String extractLocation(String result) {
      return null;
    }

    @Override
    protected String extractContent(String result) {
      return null;
    }
  }
}
