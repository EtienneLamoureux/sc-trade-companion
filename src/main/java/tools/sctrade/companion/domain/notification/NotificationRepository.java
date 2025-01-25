package tools.sctrade.companion.domain.notification;

/**
 * Notification repository interface. To be implemented by a concrete output port.
 */
public interface NotificationRepository {
  /**
   * Adds a notification to the repository.
   *
   * @param level The level of the notification.
   * @param message The message of the notification
   */
  void add(NotificationLevel level, String message);
}
