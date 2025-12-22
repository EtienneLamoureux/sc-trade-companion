package tools.sctrade.companion.domain.commodity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.exceptions.NoCloseStringException;

/**
 * Extracts the type of transaction from a screen capture.
 */
public class TransactionTypeExtractor {
  private final Logger logger = LoggerFactory.getLogger(TransactionTypeExtractor.class);
  static final String SHOP_QUANTITY = "shop quantity";


  /**
   * Constructor.
   */
  public TransactionTypeExtractor() {}

  /**
   * Extracts the type of transaction from a screen capture.
   *
   * @param result the OCR result from the left side of the screen. See
   *        {@link CommodityLocationReader}
   * @return the type of transaction
   */
  public TransactionType extract(OcrResult result) {
    try {
      OcrUtil.findFragmentClosestTo(result, SHOP_QUANTITY);

      return TransactionType.SELLS;
    } catch (NoCloseStringException e) {
      return TransactionType.BUYS;
    }
  }
}
