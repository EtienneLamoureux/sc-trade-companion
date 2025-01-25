package tools.sctrade.companion.utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A utility class that provides methods for getting localized strings.
 */
public class LocalizationUtil {
  private LocalizationUtil() {}

  private static ResourceBundle bundle =
      ResourceBundle.getBundle("bundles.localization", Locale.getDefault());

  /**
   * Returns the localized string for the given key.
   *
   * @param key The key for the string to be returned.
   * @return The localized string for the given key.
   */
  public static String get(String key) {
    return bundle.getString(key);
  }
}
