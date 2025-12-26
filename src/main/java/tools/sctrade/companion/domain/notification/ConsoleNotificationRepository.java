package tools.sctrade.companion.domain.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleNotificationRepository implements NotificationRepository {
  private final Logger logger = LoggerFactory.getLogger(ConsoleNotificationRepository.class);

  @Override
  public void add(NotificationLevel level, String message) {
    switch (level) {
      case ERROR:
        logger.error(message);
        break;
      case WARN:
        logger.warn(message);
        break;
      default:
      case INFO:
        logger.info(message);
    }
  }

}
