package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.List;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.TesseractOcr;

public class CommodityTesseractOcr extends TesseractOcr {
  private final Logger logger = LoggerFactory.getLogger(CommodityTesseractOcr.class);

  public CommodityTesseractOcr(List<ImageManipulation> preprocessingManipulations) {
    super(preprocessingManipulations);
  }

  @Override
  public String process(BufferedImage image) {
    try {
      String text = tesseract.doOCR(image);
      return text;
    } catch (TesseractException e) {
      logger.error("Error wile doing OCR with Tesseract", e);

      return "";
    }
  }
}
