package tools.sctrade.companion.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import tools.sctrade.companion.domain.notification.NotificationService;

public abstract class AsynchronousProcessor<T> {
  private final Logger logger = LoggerFactory.getLogger(AsynchronousProcessor.class);

  protected NotificationService notificationService;

  protected AsynchronousProcessor(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Async
  public void processAsynchronously(T unitOfWork) {
    try {
      process(unitOfWork);
    } catch (Exception e) {
      logger.error("Error while processing", e);
      notificationService.error(e);
    }
  }

  protected abstract void process(T unitOfWork) throws Exception;
}
