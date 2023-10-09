package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.TesseractOcr;

public class CommodityTesseractOcr extends TesseractOcr {
  private final Logger logger = LoggerFactory.getLogger(CommodityTesseractOcr.class);

  @Override
  public String read(BufferedImage image) {
    try {
      String text = tesseract.doOCR(image);
      return text;
    } catch (TesseractException e) {
      logger.error("Error wile doing OCR with Tesseract", e);

      return "";
    }

  }

}
