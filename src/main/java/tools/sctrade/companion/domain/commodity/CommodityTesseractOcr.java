package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import net.sourceforge.tess4j.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.ocr.LocatedWord;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.TesseractOcr;

public class CommodityTesseractOcr extends TesseractOcr {
  private final Logger logger = LoggerFactory.getLogger(CommodityTesseractOcr.class);

  public CommodityTesseractOcr(List<ImageManipulation> preprocessingManipulations) {
    super(preprocessingManipulations);

    tesseract.setConfigs(Arrays.asList("commodity"));
  }

  @Override
  public OcrResult process(BufferedImage image) {
    var words = tesseract.getWords(image, 0);
    words = onlyKeepWordsInRightHalfOfImage(image, words);
    words = removeWordsRightOfTheListings(words);

    return new OcrResult(
        words.stream().map(n -> new LocatedWord(n.getText(), n.getBoundingBox())).toList());
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

  private boolean isInListings(Word word) {
    String string = word.getText().toLowerCase().strip();

    return string.endsWith("/unit") || string.endsWith("scu");
  }
}
