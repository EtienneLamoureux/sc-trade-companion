package tools.sctrade.companion.domain.image;

import java.awt.image.BufferedImage;
import java.util.List;

public abstract class Ocr {
  private List<ImageManipulation> preprocessingManipulations;

  protected Ocr(List<ImageManipulation> preprocessingManipulations) {
    this.preprocessingManipulations = preprocessingManipulations;
  }

  public final String read(BufferedImage image) {
    image = preProcess(image);

    return process(image);
  }

  protected abstract String process(BufferedImage image);

  private BufferedImage preProcess(BufferedImage image) {
    for (var manipulation : preprocessingManipulations) {
      image = manipulation.manipulate(image);
    }

    return image;
  }
}
