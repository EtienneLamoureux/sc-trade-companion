package tools.sctrade.companion.exceptions;

import java.util.Locale;
import tools.sctrade.companion.domain.ocr.LocatedColumn;

public class LocationNotFoundException extends RuntimeException {
  private static final long serialVersionUID = -2418133626570478149L;

  public LocationNotFoundException(LocatedColumn column) {
    super(String.format(Locale.ROOT, "Could not extract location from: %s", column.getText()));
  }

}
