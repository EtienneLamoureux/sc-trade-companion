package tools.sctrade.companion.exceptions;

import java.util.Locale;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Exception thrown when no nearby (Levenshtein distance) string could be found
 */
public class NoCloseStringException extends RuntimeException {
  private static final long serialVersionUID = -1310636886842761669L;

  /**
   * Creates a new instance of the NoCloseStringException class.
   * 
   * @param string The string for which no close match were found
   */
  public NoCloseStringException(String string) {
    super(String.format(Locale.ROOT, LocalizationUtil.get("errorNoCloseString"), string));
  }
}
