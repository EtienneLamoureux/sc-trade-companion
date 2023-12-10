package tools.sctrade.companion.domain.notification;

public interface NotificationRepository {
  void add(NotificationLevel level, String message);
}
