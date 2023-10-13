package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
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

    tesseract.setConfigs(Arrays.asList("commodity"));
  }

  @Override
  public String process(BufferedImage image) {
    try {
      Rectangle rectangleOfInterest =
          new Rectangle(image.getWidth() / 2, 0, image.getWidth() / 2, image.getHeight());
      String text = tesseract.doOCR(image, rectangleOfInterest);
      return text;
    } catch (TesseractException e) {
      logger.error("Error wile doing OCR with Tesseract", e);

      return "";
    }
  }
}
