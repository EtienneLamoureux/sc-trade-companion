package tools.sctrade.companion.gui.screenshot;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

/**
 * Enumerates the possible processing statuses of a {@link Screenshot}.
 */
public enum ScreenshotStatus {
  QUEUED(MaterialDesign.MDI_CLOCK, "In queue", "screenshot-status-muted"), PROCESSING(
      MaterialDesign.MDI_CLOCK, "Processing...", "screenshot-status-normal"), SUCCESS(
          MaterialDesign.MDI_CHECK_CIRCLE_OUTLINE, "Success", "screenshot-status-success"), ERROR(
              MaterialDesign.MDI_ALERT_CIRCLE_OUTLINE, "Error", "screenshot-status-danger");

  private final Ikon icon;
  private final String defaultText;
  private final String styleClass;

  ScreenshotStatus(Ikon icon, String defaultText, String styleClass) {
    this.icon = icon;
    this.defaultText = defaultText;
    this.styleClass = styleClass;
  }

  /**
   * Returns the icon used to render this status.
   *
   * @return icon.
   */
  public Ikon icon() {
    return icon;
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
