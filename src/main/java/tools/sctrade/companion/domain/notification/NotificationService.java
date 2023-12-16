package tools.sctrade.companion.domain.notification;

public class NotificationService {
  private NotificationRepository repository;

  public NotificationService(NotificationRepository repository) {
    this.repository = repository;
  }

  public void info(String string) {
    repository.add(NotificationLevel.INFO, string);
  }

  public void warn(String string) {
    repository.add(NotificationLevel.WARN, string);
  }

  public void error(String string) {
    repository.add(NotificationLevel.ERROR, string);
  }

  public void error(Exception e) {
    repository.add(NotificationLevel.ERROR, e.getMessage());
  }
}
