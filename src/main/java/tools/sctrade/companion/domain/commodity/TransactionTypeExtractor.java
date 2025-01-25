package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.exceptions.NoCloseStringException;

/**
 * Extracts the type of transaction from a screen capture.
 */
public class TransactionTypeExtractor {
  private final Logger logger = LoggerFactory.getLogger(TransactionTypeExtractor.class);
  static final String SHOP_QUANTITY = "shop quantity";

  private ImageWriter imageWriter;

  /**
   * Constructor.
   *
   * @param imageWriter the image writer
   */
  public TransactionTypeExtractor(ImageWriter imageWriter) {
    this.imageWriter = imageWriter;
  }

  /**
   * Extracts the type of transaction from a screen capture.
   *
   * @param screenCapture the screen capture
   * @param result the OCR result from the left side of the screen. See
   *        {@link CommodityLocationReader}
   * @return the type of transaction
   */
  public TransactionType extract(BufferedImage screenCapture, OcrResult result) {
    try {
      OcrUtil.findFragmentClosestTo(result, SHOP_QUANTITY);

      return TransactionType.SELLS;
    } catch (NoCloseStringException e) {
      return TransactionType.BUYS;
    }
  }
}
