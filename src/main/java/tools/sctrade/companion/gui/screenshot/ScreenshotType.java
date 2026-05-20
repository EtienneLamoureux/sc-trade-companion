package tools.sctrade.companion.gui.screenshot;

/**
 * Enumerates the recognisable types of {@link Screenshot}.
 */
public enum ScreenshotType {
  COMMODITY_KIOSK("Commodity kiosk"), ITEM_KIOSK("Item kiosk");

  private final String label;

  ScreenshotType(String label) {
    this.label = label;
  }

  /** Returns a human-readable label suitable for display in the UI. */
  public String label() {
    return label;
  }
}
