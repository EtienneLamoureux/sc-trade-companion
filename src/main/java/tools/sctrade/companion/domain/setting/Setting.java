package tools.sctrade.companion.domain.setting;

import java.nio.file.Paths;

/**
 * Enumerates the settings that can be configured.
 */
public enum Setting {
  OUTPUT_SCREENSHOTS, OUTPUT_TRANSIENT_IMAGES, MY_IMAGES_PATH, MY_DATA_PATH, SC_TRADE_TOOLS_ROOT_URL, USERNAME, STAR_CITIZEN_LIVE_PATH, STAR_CITIZEN_MONITOR, PRINTSCREEN_KEYBIND;

  /**
   * Casts a string value to the appropriate type for the setting.
   *
   * @param <T> Type to cast to.
   * @param value String value to cast.
   * @return Casted value.
   */
  @SuppressWarnings("unchecked")
  public <T> T cast(String value) {
    switch (this) {
      case OUTPUT_SCREENSHOTS, OUTPUT_TRANSIENT_IMAGES:
        return (T) Boolean.valueOf(value);
      case MY_DATA_PATH, MY_IMAGES_PATH:
        return (T) Paths.get(value);
      case PRINTSCREEN_KEYBIND:
        return (T) Integer.valueOf(value);
      default:
        return (T) value;
    }
  }
}
