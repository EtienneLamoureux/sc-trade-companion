package tools.sctrade.companion.spring;

import java.awt.EventQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.scheduling.annotation.Async;
import tools.sctrade.companion.domain.CompanionVersionRepository;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.gui.CompanionGui;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Checks whether a newer version of SC Trade Companion is available and shows a one-time popup when
 * an update is found.
 */
public class CompanionVersionChecker {
  private final CompanionVersionRepository repository;
  private final CompanionGui gui;
  private final NotificationService notificationService;
  private final String currentVersion;
  private final AtomicBoolean popupAlreadyShown = new AtomicBoolean(false);

  /**
   * Constructs a new {@link CompanionVersionChecker}.
   *
   * @param repository the repository used to fetch the latest published version
   * @param gui the companion GUI, used to show the update popup on the EDT
   * @param notificationService the notification service, used to warn when the check fails
   * @param currentVersion the version of the currently running application
   */
  public CompanionVersionChecker(CompanionVersionRepository repository, CompanionGui gui,
      NotificationService notificationService, String currentVersion) {
    this.repository = repository;
    this.gui = gui;
    this.notificationService = notificationService;
    this.currentVersion = currentVersion;
  }

  /**
   * Asynchronously delegates to {@link #check()}.
   */
  @Async
  public void checkAsynchronously() {
    check();
  }

  /**
   * Fetches the latest version and shows a one-time update popup on the EDT when the current
   * version differs from the latest. If the fetch fails, a warning is logged via the notification
   * service and no popup is shown.
   */
  public void check() {
    if (popupAlreadyShown.get()) {
      return;
    }

    try {
      String latestVersion = repository.fetchLatestVersion();

      if (!currentVersion.equals(latestVersion) && popupAlreadyShown.compareAndSet(false, true)) {
        EventQueue.invokeLater(() -> gui.showUpdateAvailablePopup(currentVersion, latestVersion));
      }
    } catch (RuntimeException e) {
      notificationService.warn(LocalizationUtil.get("warningUnableToCheckLatestVersion"));
    }
  }
}
