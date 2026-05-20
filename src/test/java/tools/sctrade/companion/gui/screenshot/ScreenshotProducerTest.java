package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.exceptions.ThreadingException;
import tools.sctrade.companion.gui.screenshot.ScreenshotProducer;
import tools.sctrade.companion.gui.screenshot.ScreenshotRepository;
import tools.sctrade.companion.gui.screenshot.ScreenshotSta
import tools.sctrade.companion.gui.screenshot.ScreenshotType;

class ScreenshotProducerTest {

  private static final ScreenshotType TYPE = ScreenshotType.COMMODITY_KIOSK;

  private ArrayBlockingQueue<BufferedImage> queue;
  private ScreenshotRepository screenshotRepository;
  private TestablescreenshotProducer producer;
  private BufferedImage image;

  @BeforeEach
  void setUp() {
    queue = new ArrayBlockingQueue<>(10);
    screenshotRepository = new ScreenshotRepository();
    producer = new TestablescreenshotProducer(queue, screenshotRepository, TYPE);
    image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
  }

  @Test
  void whenProducingImage_thenScreenshotIsUpsertedWithQueuedStatus() {
    producer.callProduce(image);

    assertEquals(ScreenshotStatus.QUEUED, screenshotRepository.getSnapshot().get(0).status());
  }

  @Test
  void whenProducingImage_thenScreenshotIsUpsertedWithExpectedType() {
    producer.callProduce(image);

    assertEquals(TYPE, screenshotRepository.getSnapshot().get(0).type());
  }

  @Test
  void whenProducingImage_thenScreenshotIsUpsertedWithEllipsisLocation() {
    producer.callProduce(image);

    assertEquals("...", screenshotRepository.getSnapshot().get(0).location());
  }

  @Test
  void whenProducingImage_thenImageIsAddedToQueue() {
    producer.callProduce(image);

    assertEquals(1, queue.size());
  }

  @Test
  void givenEnqueueThrows_whenProducing_thenRepositoryRemainsEmpty() {
    ArrayBlockingQueue<BufferedImage> fullQueue = new ArrayBlockingQueue<>(1);
    fullQueue.offer(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    ScreenshotRepository emptyRepo = new ScreenshotRepository();
    TestablescreenshotProducer throwingProducer =
        new TestablescreenshotProducer(fullQueue, emptyRepo, TYPE);

    Thread.currentThread().interrupt();
    try {
      assertThrows(ThreadingException.class, () -> throwingProducer.callProduce(image));
    } finally {
      Thread.interrupted(); // clear interrupt status
    }

    assertEquals(0, emptyRepo.getSnapshot().size());
  }

  /**
   * Test subclass that exposes {@code produce()} so tests do not need to be in the same package as
   * {@link tools.sctrade.companion.utils.patterns.Producer}.
   */
  private static class TestablescreenshotProducer extends ScreenshotProducer {
    TestablescreenshotProducer(BlockingQueue<BufferedImage> queue,
        ScreenshotRepository screenshotRepository, ScreenshotType screenshotType) {
      super(queue, screenshotRepository, screenshotType);
    }

    void callProduce(BufferedImage image) {
      produce(image);
    }
  }
}
