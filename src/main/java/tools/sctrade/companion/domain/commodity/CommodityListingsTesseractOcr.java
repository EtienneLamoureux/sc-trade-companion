package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import net.sourceforge.tess4j.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.ocr.LocatedWord;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.TesseractOcr;

public class CommodityListingsTesseractOcr extends TesseractOcr {
  private final Logger logger = LoggerFactory.getLogger(CommodityListingsTesseractOcr.class);

  public CommodityListingsTesseractOcr(List<ImageManipulation> preprocessingManipulations) {
    super(preprocessingManipulations);
  }

  @Override
  public OcrResult process(BufferedImage image) {
    var words = tesseract.getWords(image, 0);
    words = removeSingleCharacterWords(words);
    words = onlyKeepWordsInRightHalfOfImage(image, words);
    words = removeWordsRightOfTheListings(words);
    words = removeWordsBelowTheListings(words);
    logger.trace(
        words.stream().map(n -> n.getText()).collect(Collectors.joining(System.lineSeparator())));

    return new OcrResult(words.stream()
        .map(n -> new LocatedWord(n.getText().toLowerCase(), n.getBoundingBox())).toList());
  }

  private List<Word> onlyKeepWordsInRightHalfOfImage(BufferedImage image, List<Word> words) {
    Rectangle rightHalf =
        new Rectangle((image.getWidth() / 2), 0, (image.getWidth() / 2), image.getHeight());
    return words.stream().filter(n -> rightHalf.contains(n.getBoundingBox())).toList();
  }

  private List<Word> removeWordsRightOfTheListings(List<Word> words) {
    OptionalDouble maxX = words.parallelStream().filter(n -> isInListings(n))
        .map(n -> (n.getBoundingBox().getMaxX() + (n.getBoundingBox().getWidth() / 2.0)))
        .mapToDouble(n -> n).max();

    if (maxX.isEmpty()) {
      return words;
    }

    return words.stream().filter(n -> n.getBoundingBox().getMaxX() <= maxX.getAsDouble()).toList();
  }

  private List<Word> removeWordsBelowTheListings(List<Word> words) {
    OptionalDouble maxY = words.parallelStream().filter(n -> isInListings(n))
        .map(n -> (n.getBoundingBox().getMaxY() + (n.getBoundingBox().getHeight() * 2)))
        .mapToDouble(n -> n).max();

    if (maxY.isEmpty()) {
      return words;
    }

    return words.stream().filter(n -> n.getBoundingBox().getMaxY() <= maxY.getAsDouble()).toList();
  }

  private List<Word> removeSingleCharacterWords(List<Word> words) {
    return words.stream().filter(n -> n.getText().strip().length() > 1).toList();
  }

  private boolean isInListings(Word word) {
    String string = word.getText().toLowerCase().strip();

    return string.endsWith("/unit") || string.endsWith("scu") || string.endsWith("stock")
        || string.startsWith("out") || string.endsWith("demand");
  }
}
