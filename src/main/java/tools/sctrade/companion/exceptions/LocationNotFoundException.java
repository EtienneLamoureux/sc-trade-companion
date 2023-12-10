package tools.sctrade.companion.exceptions;

import java.util.Locale;
import tools.sctrade.companion.domain.ocr.LocatedColumn;
import tools.sctrade.companion.utils.LocalizationUtil;

public class LocationNotFoundException extends RuntimeException {
  private static final long serialVersionUID = -2418133626570478149L;

  public LocationNotFoundException(LocatedColumn column) {
    super(String.format(Locale.ROOT, LocalizationUtil.get("errorLocationNotFound"),
        column.getText()));
  }

}
