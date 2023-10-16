package tools.sctrade.companion.ocr.tesseract;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.ocr.LocatedWord;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.ocr.TesseractOcr;

public class CommodityTesseractOcr extends TesseractOcr {
  private final Logger logger = LoggerFactory.getLogger(CommodityTesseractOcr.class);

  public CommodityTesseractOcr(List<ImageManipulation> preprocessingManipulations) {
    super(preprocessingManipulations);

    tesseract.setConfigs(Arrays.asList("commodity"));
  }

  @Override
  public OcrResult process(BufferedImage image) {
    Rectangle rectangleOfInterest =
        new Rectangle(image.getWidth() / 2, 0, image.getWidth() / 2, image.getHeight());
    var words = tesseract.getWords(image, 0);
    OcrResult result = new OcrResult();
    words.stream().filter(n -> rectangleOfInterest.contains(n.getBoundingBox()))
        .map(n -> new LocatedWord(n.getText(), n.getBoundingBox())).forEach(n -> result.add(n));

    return result;
  }
}
