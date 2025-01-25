package tools.sctrade.companion.domain.ocr;

import java.awt.image.BufferedImage;
import java.util.List;
import tools.sctrade.companion.domain.image.ImageManipulation;

/**
 * Abstract class for Optical Character Recognition (OCR) operations.
 */
public abstract class Ocr {
  private List<ImageManipulation> preprocessingManipulations;

  /**
   * Creates a new instance of the Ocr class.
   *
   * @param preprocessingManipulations The list of image manipulations to apply, in order, before
   *        processing.
   */
  protected Ocr(List<ImageManipulation> preprocessingManipulations) {
    this.preprocessingManipulations = preprocessingManipulations;
  }

  /**
   * Reads the text from an image.
   *
   * @param image The image to read.
   * @return The OCR result.
   */
  public final OcrResult read(BufferedImage image) {
    image = preProcess(image);

    return process(image);
  }

  /**
   * Processes the image to extract the text.
   *
   * @param image The preprocessed image.
   * @return The OCR result.
   */
  protected abstract OcrResult process(BufferedImage image);

  private BufferedImage preProcess(BufferedImage image) {
    for (var manipulation : preprocessingManipulations) {
      image = manipulation.manipulate(image);
    }

    return image;
  }
}
