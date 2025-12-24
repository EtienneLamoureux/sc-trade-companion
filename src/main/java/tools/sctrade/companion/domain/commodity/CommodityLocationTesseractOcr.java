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
    Rectangle locationRegion = calculateLocationRegion(image);
    return ImageUtil.crop(image, locationRegion);
  }

  /**
   * Calculates the region containing the location dropdown based on aspect ratio. Uses conservative
   * wide crops for ultrawide monitors to handle viewing angle variance.
   *
   * @param image The source image
   * @return Rectangle defining the region to crop
   */
  private Rectangle calculateLocationRegion(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();
    double aspectRatio = (double) width / height;

    // Super ultrawide (32:9, ratio > 3.0)
    if (aspectRatio > 3.0) {
      // Crop from 0% to 70% width, top 40% height
      return new Rectangle(0, 0, (int) (width * 0.70), (int) (height * 0.40));
    }

    // Ultrawide (21:9, ratio > 2.0)
    if (aspectRatio > 2.0) {
      // Crop from 0% to 75% width, top 40% height
      return new Rectangle(0, 0, (int) (width * 0.75), (int) (height * 0.40));
    }

    // Standard/widescreen - original behavior
    return new Rectangle(0, 0, width / 2, height / 3);
  }
}
