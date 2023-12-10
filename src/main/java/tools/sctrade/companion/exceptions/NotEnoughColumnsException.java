package tools.sctrade.companion.exceptions;

import java.util.Locale;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.utils.LocalizationUtil;

public class NotEnoughColumnsException extends RuntimeException {
  private static final long serialVersionUID = -850092263296022156L;

  public NotEnoughColumnsException(int minColumnCount, OcrResult result) {
    super(String.format(Locale.ROOT, LocalizationUtil.get("errorNotEnoughColumns"), minColumnCount,
        result.getTextByColumns()));
  }

}
