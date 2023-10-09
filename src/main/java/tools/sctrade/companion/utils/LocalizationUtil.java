package tools.sctrade.companion.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationUtil {
  private LocalizationUtil() {}

  private static ResourceBundle bundle =
      ResourceBundle.getBundle("bundles.localization", Locale.getDefault());

  public static String get(String key) {
    return bundle.getString(key);
  }
}
