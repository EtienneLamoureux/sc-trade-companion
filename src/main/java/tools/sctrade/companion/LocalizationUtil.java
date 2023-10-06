package tools.sctrade.companion;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationUtil {
  private static ResourceBundle bundle =
      ResourceBundle.getBundle("bundles.localization", Locale.getDefault());

  public static String getString(String key) {
    return bundle.getString(key);
  }
}
