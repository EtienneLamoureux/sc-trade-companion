package tools.sctrade.companion.domain.ocr;

import java.util.Collections;
import java.util.List;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import tools.sctrade.companion.domain.image.ImageManipulation;

public abstract class TesseractOcr extends Ocr {
  protected Tesseract tesseract;

  protected TesseractOcr() {
    this(Collections.emptyList());
  }

  protected TesseractOcr(List<ImageManipulation> preprocessingManipulations) {
    super(preprocessingManipulations);

    this.tesseract = new Tesseract();
    tesseract.setDatapath("src/main/resources/tessdata");
    tesseract.setLanguage("eng");
    tesseract.setPageSegMode(11);
    tesseract.setOcrEngineMode(3);
  }

  protected List<Word> removeSingleCharacterWords(List<Word> words) {
    return words.stream().filter(n -> n.getText().strip().length() > 1).toList();
  }

  protected List<Word> removeNonWords(List<Word> words) {
    return words.stream().filter(n -> !n.getText().matches("[^a-zA-Z0-9]+")).toList();
  }
}
