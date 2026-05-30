package tools.sctrade.companion.domain.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ImageTypeTest {

  @Test
  void whenGeneratingConcurrentFileNamesThenNamesAreUnique() throws InterruptedException {
    int threadCount = 100;
    Set<String> names = ConcurrentHashMap.newKeySet();
    CountDownLatch ready = new CountDownLatch(threadCount);
    CountDownLatch done = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      new Thread(() -> {
        try {
          ready.countDown();
          ready.await();
          names.add(ImageType.SCREENSHOT.generateFileName());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          done.countDown();
        }
      }).start();
    }

    assertTrue(done.await(10, TimeUnit.SECONDS));
    assertEquals(threadCount, names.size(), "Duplicate filenames detected under concurrency");
  }
}
