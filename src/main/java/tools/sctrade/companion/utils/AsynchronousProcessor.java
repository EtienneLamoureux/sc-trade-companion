package tools.sctrade.companion.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import tools.sctrade.companion.domain.notification.NotificationService;

/**
 * Base class for processing units of work asynchronously.
 *
 * @param <T> the type of the unit of work
 */
public abstract class AsynchronousProcessor<T> {
  private final Logger logger = LoggerFactory.getLogger(AsynchronousProcessor.class);

  protected NotificationService notificationService;

  /**
   * Creates a new instance of the {@link AsynchronousProcessor} class.
   *
   * @param notificationService the notification service
   */
  protected AsynchronousProcessor(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /**
   * Processes the unit of work asynchronously.
   *
   * @param unitOfWork the unit of work to process
   */
  @Async
  public void processAsynchronously(T unitOfWork) {
    try {
      process(unitOfWork);
    } catch (Exception e) {
      logger.error("Error while processing", e);
      notificationService.error(e);
    }
  }

  /**
   * Processes the unit of work.
   *
   * @param unitOfWork the unit of work to process
   * @throws Exception if an error occurs while processing
   */
  protected abstract void process(T unitOfWork) throws Exception;
}
