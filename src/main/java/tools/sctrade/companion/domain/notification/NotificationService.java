package tools.sctrade.companion.domain.notification;

/**
 * Notification service. Used to send notifications to the user via the "logs" tab of the UI.
 */
public class NotificationService {
  private NotificationRepository repository;

  /**
   * Constructs a new notification service.
   *
   * @param repository The repository to send notifications to.
   */
  public NotificationService(NotificationRepository repository) {
    this.repository = repository;
  }

  /**
   * Sends an informational message.
   *
   * @param string The message to send.
   */
  public void info(String string) {
    repository.add(NotificationLevel.INFO, string);
  }

  /**
   * Sends a warning message.
   *
   * @param string The message to send.
   */
  public void warn(String string) {
    repository.add(NotificationLevel.WARN, string);
  }

  /**
   * Sends an error message.
   *
   * @param string The message to send.
   */
  public void error(String string) {
    repository.add(NotificationLevel.ERROR, string);
  }

  /**
   * Sends an error message.
   *
   * @param e The exception to log
   */
  public void error(Exception e) {
    repository.add(NotificationLevel.ERROR, e.getMessage());
  }
}
