package tools.sctrade.companion.domain.image;

import net.sourceforge.tess4j.Tesseract;

public abstract class TesseractOcr extends Ocr {
  protected Tesseract tesseract;

  public TesseractOcr(OcrConfiguration configuration) {
    super(configuration);

    this.tesseract = new Tesseract();
    tesseract.setDatapath("src/main/resources/tessdata");
    tesseract.setLanguage("eng");
    tesseract.setPageSegMode(11);
    tesseract.setOcrEngineMode(1);
  }

}
