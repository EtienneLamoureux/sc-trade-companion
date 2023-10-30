package tools.sctrade.companion.exceptions;

import java.util.Locale;

public class NoCloseStringException extends RuntimeException {
  private static final long serialVersionUID = -1310636886842761669L;

  public NoCloseStringException(String string) {
    super(String.format(Locale.ROOT, "Could not find string resembling '%s'", string));
  }
}
