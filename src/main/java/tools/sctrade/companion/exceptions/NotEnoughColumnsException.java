package tools.sctrade.companion.exceptions;

import java.util.Locale;
import tools.sctrade.companion.domain.ocr.OcrResult;

public class NotEnoughColumnsException extends RuntimeException {
  private static final long serialVersionUID = -850092263296022156L;

  public NotEnoughColumnsException(int minColumnCount, OcrResult result) {
    super(String.format(Locale.ROOT, "Could not make out %d or more column(s) of text in:%n%s",
        minColumnCount, result.getTextByColumns()));
  }

}
