package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.SubmissionFactory;
import tools.sctrade.companion.domain.item.ItemSubmission;
import tools.sctrade.companion.domain.user.User;
import tools.sctrade.companion.utils.LocalizationUtil;

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

  @Test
  void whenItemSubmissionIsEmpty_thenRepositoryHasErrorStatusWithNoLocationWarning() {
    SubmissionFactory<ItemSubmission> delegate =
        screenCapture -> new ItemSubmission(new User("id", "label"), List.of());
    var factory = new StatusTrackingSubmissionFactory<>(delegate, screenshotRepository,
        ScreenshotType.ITEM_KIOSK, new ScreenshotFactory());

    factory.build(image);

    assertEquals(ScreenshotStatus.ERROR, screenshotRepository.getSnapshot().get(0).status());
    assertEquals(LocalizationUtil.get("warnNoLocation"),
        screenshotRepository.getSnapshot().get(0).error());
  }

  @Test
  void givenProducerAndConsumerForSameImageType_whenProcessing_thenSingleScreenshotRecordIsUpdated() {
    BlockingQueue<BufferedImage> queue = new ArrayBlockingQueue<>(5);
    TestableScreenshotProducer producer =
        new TestableScreenshotProducer(queue, screenshotRepository, ScreenshotType.ITEM_KIOSK);
    SubmissionFactory<String> delegate = screenCapture -> "result";
    var factory = new StatusTrackingSubmissionFactory<>(delegate, screenshotRepository,
        ScreenshotType.ITEM_KIOSK, new ScreenshotFactory());

    producer.callProduce(image);
    factory.build(image);

    assertEquals(1, screenshotRepository.getSnapshot().size());
  }

  /**
   * Test subclass that exposes {@code produce()} so tests do not need to be in the same package as
   * {@link tools.sctrade.companion.utils.patterns.Producer}.
   */
  private static class TestableScreenshotProducer extends ScreenshotProducer {
    TestableScreenshotProducer(BlockingQueue<BufferedImage> queue,
        ScreenshotRepository screenshotRepository, ScreenshotType screenshotType) {
      super(queue, screenshotRepository, screenshotType);
    }

    void callProduce(BufferedImage image) {
      produce(image);
    }
  }
}
