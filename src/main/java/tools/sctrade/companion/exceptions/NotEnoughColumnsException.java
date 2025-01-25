package tools.sctrade.companion.exceptions;

import java.util.Locale;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Exception thrown when the number of columns in the OCR result is less than expected.
 */
public class NotEnoughColumnsException extends RuntimeException {
  private static final long serialVersionUID = -850092263296022156L;

  /**
   * Creates a new instance of the exception.
   * 
   * @param minColumnCount the minimum number of columns expected
   * @param result the OCR result
   */
  public NotEnoughColumnsException(int minColumnCount, OcrResult result) {
    super(String.format(Locale.ROOT, LocalizationUtil.get("errorNotEnoughColumns"), minColumnCount,
        result.getTextByColumns()));
  }

}
