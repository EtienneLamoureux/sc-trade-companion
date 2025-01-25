package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.ocr.LocatedWord;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.TesseractOcr;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * OCR implementation for extracting commodity location from the game's screenshots.
 */
public class CommodityLocationTesseractOcr extends TesseractOcr {
  private final Logger logger = LoggerFactory.getLogger(CommodityLocationTesseractOcr.class);

  /**
   * Constructor.
   *
   * @param preprocessingManipulations List of image manipulations to apply before OCR, in order.
   */
  public CommodityLocationTesseractOcr(List<ImageManipulation> preprocessingManipulations) {
    super(preprocessingManipulations);
  }

  @Override
  public OcrResult process(BufferedImage image) {
    image = keepTopLeftCorner(image);
    var words = tesseract.getWords(image, 0);
    words = removeNonWords(words);
    words = removeSingleCharacterWords(words);
    logger.trace(
        words.stream().map(n -> n.getText()).collect(Collectors.joining(System.lineSeparator())));

    return new OcrResult(words.stream()
        .map(n -> new LocatedWord(n.getText().toLowerCase(Locale.ROOT), n.getBoundingBox()))
        .toList());
  }

  private BufferedImage keepTopLeftCorner(BufferedImage image) {
    return ImageUtil.crop(image,
        new Rectangle(0, 0, (image.getWidth() / 2), (image.getHeight() / 3)));
  }
}
