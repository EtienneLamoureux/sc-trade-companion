package tools.sctrade.companion.domain.version;

/**
 * Output port for showing a popup when a newer version of SC Trade Companion is available.
 */
public interface UpdateAvailablePopup {
  /**
   * Shows a modal dialog informing the user that a newer version is available.
   *
   * <p>
   * Clicking <em>Download</em> opens the GitHub releases page in the system browser. Clicking
   * <em>Later</em> dismisses the dialog without further action.
   *
   * @param currentVersion the currently running version
   * @param latestVersion the latest available version
   */
  void showUpdateAvailablePopup(String currentVersion, String latestVersion);
}
