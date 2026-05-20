package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.SubmissionFactory;

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
    SubmissionFactory<String> delegate = screenCapture -> "result";
    var factory = new StatusTrackingSubmissionFactory<>(delegate, screenshotRepository, TYPE,
        new ScreenshotFactory());

    factory.build(image);

    assertEquals(ScreenshotStatus.SUCCESS, screenshotRepository.getSnapshot().get(0).status());
  }

  @Test
  void whenBuildFails_thenRepositoryHasErrorStatus() {
    SubmissionFactory<String> delegate = screenCapture -> {
      throw new RuntimeException("error message");
    };
    var factory = new StatusTrackingSubmissionFactory<>(delegate, screenshotRepository, TYPE,
        new ScreenshotFactory());

    assertThrows(RuntimeException.class, () -> factory.build(image));

    assertEquals(ScreenshotStatus.ERROR, screenshotRepository.getSnapshot().get(0).status());
  }

  @Test
  void whenBuildFails_thenExceptionIsRethrown() {
    SubmissionFactory<String> delegate = screenCapture -> {
      throw new RuntimeException("error message");
    };
    var factory = new StatusTrackingSubmissionFactory<>(delegate, screenshotRepository, TYPE,
        new ScreenshotFactory());

    assertThrows(RuntimeException.class, () -> factory.build(image));
  }

  @Test
  void whenBuildFails_thenErrorMessageIsStoredInRepository() {
    SubmissionFactory<String> delegate = screenCapture -> {
      throw new RuntimeException("error message");
    };
    var factory = new StatusTrackingSubmissionFactory<>(delegate, screenshotRepository, TYPE,
        new ScreenshotFactory());

    assertThrows(RuntimeException.class, () -> factory.build(image));

    assertEquals("error message", screenshotRepository.getSnapshot().get(0).error());
  }

  @Test
  void whenBuildSucceeds_thenScreenshotTypeIsStoredInRepository() {
    SubmissionFactory<String> delegate = screenCapture -> "result";
    var factory = new StatusTrackingSubmissionFactory<>(delegate, screenshotRepository, TYPE,
        new ScreenshotFactory());

    factory.build(image);

    assertEquals(TYPE, screenshotRepository.getSnapshot().get(0).type());
  }
}
