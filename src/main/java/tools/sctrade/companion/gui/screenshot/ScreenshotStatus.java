package tools.sctrade.companion.gui.screenshot;

/**
 * Enumerates the possible processing statuses of a {@link Screenshot}.
 */
public enum ScreenshotStatus {
  QUEUED("⏳", "In queue", "screenshot-status-muted"), PROCESSING("⚙", "Processing...",
      "screenshot-status-normal"), SUCCESS("✅", "Success",
          "screenshot-status-success"), ERROR("⚠", "Error", "screenshot-status-danger");

  private final String glyph;
  private final String defaultText;
  private final String styleClass;

  ScreenshotStatus(String glyph, String defaultText, String styleClass) {
    this.glyph = glyph;
    this.defaultText = defaultText;
    this.styleClass = styleClass;
  }

  /**
   * Returns the glyph used to render this status.
   *
   * @return glyph.
   */
  public String glyph() {
    return glyph;
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
