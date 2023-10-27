package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.sourceforge.tess4j.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.ocr.LocatedWord;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.TesseractOcr;

public class DisplayInfoTesseractOcr extends TesseractOcr {
  private final Logger logger = LoggerFactory.getLogger(DisplayInfoTesseractOcr.class);

  public DisplayInfoTesseractOcr(List<ImageManipulation> preprocessingManipulations) {
    super(preprocessingManipulations);

    tesseract.setConfigs(Arrays.asList("commodity"));
  }

  @Override
  public OcrResult process(BufferedImage image) {
    var words = tesseract.getWords(image, 0);
    words = onlyKeepWordsTopRightCornerOfImage(image, words);
    logger.trace(
        words.stream().map(n -> n.getText()).collect(Collectors.joining(System.lineSeparator())));

    return new OcrResult(words.stream()
        .map(n -> new LocatedWord(n.getText().toLowerCase(), n.getBoundingBox())).toList());
  }

  private List<Word> onlyKeepWordsTopRightCornerOfImage(BufferedImage image, List<Word> words) {
    Rectangle topRightCorner = new Rectangle((3 * (image.getWidth() / 4)), 0,
        (image.getWidth() / 4), (image.getHeight() / 4));
    return words.stream().filter(n -> topRightCorner.contains(n.getBoundingBox())).toList();
  }
}
