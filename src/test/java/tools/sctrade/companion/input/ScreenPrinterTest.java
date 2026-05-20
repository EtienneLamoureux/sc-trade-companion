package tools.sctrade.companion.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.gui.screenshot.ScreenshotRepository;
import tools.sctrade.companion.gui.screenshot.ScreenshotStatus;
import tools.sctrade.companion.gui.screenshot.ScreenshotType;
import tools.sctrade.companion.utils.SoundUtil;

class ScreenPrinterTest {

  private ArrayBlockingQueue<BufferedImage> queue;
  private ScreenshotRepository screenshotRepository;
  private TestableScreenPrinter screenPrinter;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    queue = new ArrayBlockingQueue<>(10);
    screenshotRepository = new ScreenshotRepository();
    screenPrinter = new TestableScreenPrinter(queue, screenshotRepository,
        ScreenshotType.COMMODITY_KIOSK, mock(ImageWriter.class), mock(SoundUtil.class),
        mock(NotificationService.class), mock(SettingRepository.class));
  }

  @Test
  void whenProducingImage_thenScreenshotIsUpsertedToRepository() {
    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

    screenPrinter.callProduce(image);

    assertEquals(ScreenshotStatus.QUEUED, screenshotRepository.getSnapshot().get(0).status());
  }

  /**
   * Test subclass that exposes {@code produce()} so tests do not need to be in the same package as
   * {@link tools.sctrade.companion.utils.patterns.Producer}.
   */
  private static class TestableScreenPrinter extends ScreenPrinter {
    TestableScreenPrinter(java.util.concurrent.BlockingQueue<BufferedImage> queue,
        ScreenshotRepository screenshotRepository, ScreenshotType screenshotType,
        ImageWriter<Optional<Path>> imageWriter, SoundUtil soundPlayer,
        NotificationService notificationService, SettingRepository settings) {
      super(queue, screenshotRepository, screenshotType, imageWriter, soundPlayer,
          notificationService, settings);
    }

    void callProduce(BufferedImage image) {
      produce(image);
    }
  }
}
