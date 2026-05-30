package tools.sctrade.companion.domain.ocr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.exceptions.OcrUnavailableException;

class OcrConcurrencyTest {

  @Test
  void givenTwoConcurrentReadCallsWhenReadingThenCallsAreSerializedAndBothComplete()
      throws InterruptedException {
    AtomicInteger concurrent = new AtomicInteger(0);
    AtomicInteger maxConcurrent = new AtomicInteger(0);

    SlowOcr ocr1 = new SlowOcr(200, concurrent, maxConcurrent);
    SlowOcr ocr2 = new SlowOcr(200, concurrent, maxConcurrent);

    CountDownLatch bothReady = new CountDownLatch(2);
    CountDownLatch bothDone = new CountDownLatch(2);

    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    Thread t1 = new Thread(() -> {
      try {
        bothReady.countDown();
        bothReady.await();
        ocr1.read(image);
      } catch (Exception ignored) {
      } finally {
        bothDone.countDown();
      }
    });
    Thread t2 = new Thread(() -> {
      try {
        bothReady.countDown();
        bothReady.await();
        ocr2.read(image);
      } catch (Exception ignored) {
      } finally {
        bothDone.countDown();
      }
    });

    long start = System.currentTimeMillis();
    t1.start();
    t2.start();
    assertTrue(bothDone.await(10, TimeUnit.SECONDS));
    long elapsed = System.currentTimeMillis() - start;

    assertEquals(1, maxConcurrent.get(), "Expected at most 1 concurrent OCR call");
    assertTrue(elapsed >= 400,
        "Expected sequential execution (>=400 ms) but completed in " + elapsed + " ms");
  }

  @Test
  void givenOcrLockHeldWhenSecondReadAttemptsThenThrowsOcrUnavailableException()
      throws InterruptedException {
    CountDownLatch lockHeld = new CountDownLatch(1);
    HangingOcr hangingOcr = new HangingOcr(lockHeld);
    InstantTimeoutOcr timeoutOcr = new InstantTimeoutOcr();

    Thread lockHolder = new Thread(() -> {
      try {
        hangingOcr.read(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
      } catch (Exception ignored) {
      }
    });
    lockHolder.start();
    assertTrue(lockHeld.await(5, TimeUnit.SECONDS), "Lock-holder thread did not acquire lock");

    assertThrows(OcrUnavailableException.class,
        () -> timeoutOcr.read(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)));

    lockHolder.interrupt();
    lockHolder.join(2000);
  }

  // -------------------------------------------------------------------------
  // Test-package helpers
  // -------------------------------------------------------------------------

  private static class SlowOcr extends Ocr {
    private final long sleepMs;
    private final AtomicInteger concurrent;
    private final AtomicInteger maxConcurrent;

    SlowOcr(long sleepMs, AtomicInteger concurrent, AtomicInteger maxConcurrent) {
      super(List.of());
      this.sleepMs = sleepMs;
      this.concurrent = concurrent;
      this.maxConcurrent = maxConcurrent;
    }

    @Override
    protected OcrResult process(BufferedImage image) {
      int c = concurrent.incrementAndGet();
      maxConcurrent.updateAndGet(m -> Math.max(m, c));
      try {
        Thread.sleep(sleepMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        concurrent.decrementAndGet();
      }
      return new OcrResult(List.of());
    }
  }

  /**
   * Signals via latch when it has acquired the OCR lock (i.e., is inside process()), then blocks
   * until interrupted.
   */
  private static class HangingOcr extends Ocr {
    private final CountDownLatch lockHeld;

    HangingOcr(CountDownLatch lockHeld) {
      super(List.of());
      this.lockHeld = lockHeld;
    }

    @Override
    protected OcrResult process(BufferedImage image) {
      lockHeld.countDown();
      try {
        Thread.sleep(Long.MAX_VALUE);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("interrupted");
      }
      return new OcrResult(List.of());
    }
  }

  /** Ocr with an extremely short lock-acquisition timeout to fast-fail in tests. */
  private static class InstantTimeoutOcr extends Ocr {
    InstantTimeoutOcr() {
      super(List.of());
    }

    @Override
    protected long getOcrLockTimeoutMs() {
      return 1;
    }

    @Override
    protected OcrResult process(BufferedImage image) {
      return new OcrResult(List.of());
    }
  }
}
