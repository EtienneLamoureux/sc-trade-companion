package tools.sctrade.companion.utils;

import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.exceptions.RecoverableProcessingException;

/**
 * Abstract consumer for processing items from a blocking queue.
 *
 * <p>
 * Processes items continuously in an infinite loop. Errors retrieving items from the queue
 * (including InterruptedException) are treated as recoverable and processing continues. Errors
 * during item consumption are logged and rethrown to allow the caller to restart the consumer. This
 * distinction allows callers to implement automatic restart logic while keeping queue-access issues
 * transparent.
 */
public abstract class Consumer<T> {
  private final Logger logger = LoggerFactory.getLogger(Consumer.class);

  private BlockingQueue<T> queue;
  protected NotificationService notificationService;

  protected Consumer(BlockingQueue<T> queue, NotificationService notificationService) {
    this.queue = queue;
    this.notificationService = notificationService;
  }

  public void startConsuming() throws Exception {
    while (true) {
      try {
        T item = queue.take();
        consume(item);
      } catch (InterruptedException e) {
        logger.warn("Consumer thread interrupted; resuming processing...");
      } catch (RecoverableProcessingException e) {
        logger.warn("Recoverable error processing item", e);
        notificationService.warn(e.getMessage());
      } catch (Exception e) {
        logger.error("Error processing item", e);
        notificationService.error(e);
        throw e;
      }
    }
  }

  protected abstract void consume(T item) throws Exception;
}
