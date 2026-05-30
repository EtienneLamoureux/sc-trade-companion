package tools.sctrade.companion.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.exceptions.RecoverableProcessingException;

class ConsumerTest {
  private BlockingQueue<Integer> queue;
  private NotificationService notificationService;

  @BeforeEach
  void setUp() {
    queue = new ArrayBlockingQueue<>(5);
    notificationService = mock(NotificationService.class);
  }

  @Test
  void givenRecoverableFailureWhenConsumingThenConsumerContinuesWithNextItem() {
    queue.add(1);
    queue.add(2);
    TestConsumer consumer = new TestConsumer(queue, notificationService, true);

    RuntimeException thrown = assertThrows(RuntimeException.class, consumer::startConsuming);

    assertEquals("stop-after-second-item", thrown.getMessage());
  }

  @Test
  void givenFatalFailureWhenConsumingThenConsumerRethrowsException() {
    queue.add(1);
    TestConsumer consumer = new TestConsumer(queue, notificationService, false);

    RuntimeException thrown = assertThrows(RuntimeException.class, consumer::startConsuming);

    assertEquals("recoverable-first-item", thrown.getMessage());
  }

  private static class TestConsumer extends Consumer<Integer> {
    private final boolean recoverableFirstItem;

    TestConsumer(BlockingQueue<Integer> queue, NotificationService notificationService,
        boolean recoverableFirstItem) {
      super(queue, notificationService);
      this.recoverableFirstItem = recoverableFirstItem;
    }

    @Override
    protected void consume(Integer item) throws Exception {
      if (item == 1) {
        if (recoverableFirstItem) {
          throw new TestRecoverableException("recoverable-first-item");
        }
        throw new RuntimeException("recoverable-first-item");
      }

      throw new RuntimeException("stop-after-second-item");
    }
  }

  private static class TestRecoverableException extends RecoverableProcessingException {
    private static final long serialVersionUID = 3490673193709190556L;

    TestRecoverableException(String message) {
      super(message);
    }
  }
}
