package tools.sctrade.companion.gui.screenshot;

/**
 * Enumerates the possible processing statuses of a {@link Screenshot}.
 */
public enum ScreenshotStatus {
  QUEUED("mdoal-access_time", "In queue", "screenshot-status-muted"), PROCESSING(
      "mdoal-access_time", "Processing...", "screenshot-status-normal"), SUCCESS(
          "mdal-check_circle_outline", "Success", "screenshot-status-success"), ERROR(
              "mdsal-error_outline", "Error", "screenshot-status-danger");

  private final String iconClass;
  private final String defaultText;
  private final String styleClass;

  ScreenshotStatus(String iconClass, String defaultText, String styleClass) {
    this.iconClass = iconClass;
    this.defaultText = defaultText;
    this.styleClass = styleClass;
  }

  /**
   * Returns the icon class used to render this status.
   *
   * @return icon class.
   */
  public String iconClass() {
    return iconClass;
  }

  /**
   * Returns the default text shown for this status.
   *
   * @return default status text.
   */
  public String defaultText() {
    return defaultText;
  }

  /**
   * Returns the CSS class used to style this status.
   *
   * @return CSS style class.
   */
  public String styleClass() {
    return styleClass;
  }
}
