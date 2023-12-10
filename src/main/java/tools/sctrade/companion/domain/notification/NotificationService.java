package tools.sctrade.companion.domain.notification;

public class NotificationService {
  private NotificationRepository repository;

  public NotificationService(NotificationRepository repository) {
    this.repository = repository;
  }

  public void notify(Exception e) {
    repository.add(NotificationLevel.ERROR, e.getMessage());
  }

  public void notify(String string) {
    repository.add(NotificationLevel.INFO, string);
  }
}
