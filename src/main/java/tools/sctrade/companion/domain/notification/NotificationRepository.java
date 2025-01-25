package tools.sctrade.companion.domain.notification;

/**
 * Notification repository interface. To be implemented by a concrete output port.
 */
public interface NotificationRepository {
  void add(NotificationLevel level, String message);
}
