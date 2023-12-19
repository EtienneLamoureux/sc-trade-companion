package tools.sctrade.companion.exceptions;

import java.util.List;
import java.util.Locale;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.utils.LocalizationUtil;

public class LocationNotFoundException extends RuntimeException {
  private static final long serialVersionUID = -2418133626570478149L;

  public LocationNotFoundException(List<LocatedFragment> fragments) {
    super(String.format(Locale.ROOT, LocalizationUtil.get("errorLocationNotFound"),
        fragments.stream().map(n -> n.getText()).toList().toString()));
  }

}
