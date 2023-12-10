package tools.sctrade.companion.exceptions;

import java.util.Locale;
import tools.sctrade.companion.utils.LocalizationUtil;

public class NoCloseStringException extends RuntimeException {
  private static final long serialVersionUID = -1310636886842761669L;

  public NoCloseStringException(String string) {
    super(String.format(Locale.ROOT, LocalizationUtil.get("errorNoCloseString"), string));
  }
}
