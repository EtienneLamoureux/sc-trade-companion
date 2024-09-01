package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.exceptions.NoCloseStringException;

public class TransactionTypeExtractor {
  private final Logger logger = LoggerFactory.getLogger(TransactionTypeExtractor.class);
  static final String SHOP_QUANTITY = "shop quantity";

  private ImageWriter imageWriter;

  public TransactionTypeExtractor(ImageWriter imageWriter) {
    this.imageWriter = imageWriter;
  }

  public TransactionType extract(BufferedImage screenCapture, OcrResult result) {
    try {
      OcrUtil.findFragmentClosestTo(result, SHOP_QUANTITY);

      return TransactionType.SELLS;
    } catch (NoCloseStringException e) {
      return TransactionType.BUYS;
    }
  }
}
