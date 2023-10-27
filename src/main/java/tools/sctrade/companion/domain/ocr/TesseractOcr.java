package tools.sctrade.companion.domain.ocr;

import java.util.List;
import net.sourceforge.tess4j.Tesseract;
import tools.sctrade.companion.domain.image.ImageManipulation;

public abstract class TesseractOcr extends Ocr {
  protected Tesseract tesseract;

  protected TesseractOcr(List<ImageManipulation> preprocessingManipulations) {
    super(preprocessingManipulations);

    this.tesseract = new Tesseract();
    tesseract.setDatapath("src/main/resources/tessdata");
    tesseract.setLanguage("eng");
    tesseract.setPageSegMode(11);
    tesseract.setOcrEngineMode(1);
  }

}
