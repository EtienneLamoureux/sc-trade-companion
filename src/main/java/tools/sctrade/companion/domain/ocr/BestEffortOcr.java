package tools.sctrade.companion.domain.ocr;

import java.awt.image.BufferedImage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.output.DiskImageWriter;

/**
 * Decorator around multiple OCR implementations. Tries each engine in order and uses the first one
 * that successfully initialises. If no engine can be loaded, throws an exception to prevent the
 * application from starting in a non-functional state.
 *
 * @see OneOcr
 * @see ScreenAiOcr
 */
public class BestEffortOcr extends Ocr {
  private final Logger logger = LoggerFactory.getLogger(BestEffortOcr.class);

  private final Ocr delegate;

  /**
   * Attempts to initialise OCR engines in order: OneOcr (Windows native), then ScreenAiOcr (Chrome
   * Screen AI, cross-platform).
   *
   * @param preprocessingManipulations image manipulations applied before OCR
   * @param diskImageWriter used to persist the screenshot before passing it to the native library
   * @throws IllegalStateException if no OCR engine could be loaded
   */
  public BestEffortOcr(List<ImageManipulation> preprocessingManipulations,
      DiskImageWriter diskImageWriter) {
    super(preprocessingManipulations);
    this.delegate = loadOcr(preprocessingManipulations, diskImageWriter);
  }

  @Override
  protected OcrResult process(BufferedImage image) {
    return delegate.process(image);
  }

  private Ocr loadOcr(List<ImageManipulation> preprocessingManipulations,
      DiskImageWriter diskImageWriter) {
    try {
      logger.debug("Attempting to load OneOcr...");
      var ocr = new OneOcr(preprocessingManipulations, diskImageWriter);
      logger.info("Using OneOcr engine");
      return ocr;
    } catch (Exception | UnsatisfiedLinkError e) {
      try {
        logger.error("OneOcr unavailable, falling back to Chrome Screen AI", e);
        var ocr = new ScreenAiOcr(preprocessingManipulations);
        logger.info("Using Chrome Screen AI engine");
        return ocr;
      } catch (Exception ex) {
        logger.error("Chrome Screen AI unavailable", ex);
        throw new IllegalStateException(
            "No OCR engine could be loaded. On Windows, ensure oneocr.dll is present in bin/oneocr."
                + " On Linux/macOS, ensure an internet connection is available for downloading"
                + " Chrome Screen AI model files.",
            ex);
      }
    }
  }
}
