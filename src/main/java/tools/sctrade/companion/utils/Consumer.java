package tools.sctrade.companion.utils;

import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.exceptions.ThreadingException;

public abstract class Consumer<T> {
  private final Logger logger = LoggerFactory.getLogger(Consumer.class);

  private BlockingQueue<T> queue;
  protected NotificationService notificationService;

  protected Consumer(BlockingQueue<T> queue, NotificationService notificationService) {
    this.queue = queue;
    this.notificationService = notificationService;
  }

  protected void startConsuming() {
    while (true) {
      T item = waitForAndGetNextItem();

      try {
        consume(item);
      } catch (Exception e) {
        logger.error("Error while processing", e);
        notificationService.error(e);
      }
    }
  }

  protected abstract void consume(T item);

  private T waitForAndGetNextItem() {
    try {
      return queue.take();
    } catch (InterruptedException e) {
      throw new ThreadingException(e);
    }
  }
}
