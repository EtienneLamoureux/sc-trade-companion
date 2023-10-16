package tools.sctrade.companion.domain.ocr;

import java.awt.image.BufferedImage;
import java.util.List;
import tools.sctrade.companion.domain.image.ImageManipulation;

public abstract class Ocr {
  private List<ImageManipulation> preprocessingManipulations;

  protected Ocr(List<ImageManipulation> preprocessingManipulations) {
    this.preprocessingManipulations = preprocessingManipulations;
  }

  public final OcrResult read(BufferedImage image) {
    image = preProcess(image);

    return process(image);
  }

  protected abstract OcrResult process(BufferedImage image);

  private BufferedImage preProcess(BufferedImage image) {
    for (var manipulation : preprocessingManipulations) {
      image = manipulation.manipulate(image);
    }

    return image;
  }
}
