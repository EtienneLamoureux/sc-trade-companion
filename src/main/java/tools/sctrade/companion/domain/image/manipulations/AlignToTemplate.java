package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Aligns an image to a reference template using homography transformation. This manipulation uses
 * ORB feature detection on the blue channel to find matching points between the input image and a
 * reference template, then applies a perspective transformation to align the image.
 */
public class AlignToTemplate implements ImageManipulation {
  private static final String TEMPLATE_PATH = "/images/ocr/commodity_kiosk_template.jpg";
  private static final double MIN_SIMILARITY_TRESHOLD = 0.12;

  private final BufferedImage template;
  private final double minSimilarityThreshold;

  /**
   * Creates a new AlignToTemplate manipulation using the default template and validation threshold.
   */
  public AlignToTemplate() {
    this(TEMPLATE_PATH, MIN_SIMILARITY_TRESHOLD);
  }

  /**
   * Creates a new AlignToTemplate manipulation with a custom template and validation threshold.
   *
   * @param templateImage The template image to align to.
   * @param minSimilarityThreshold Minimum similarity score (0.0 to 1.0) for the alignment to be
   *        considered valid. If the aligned image's similarity to the template is below this
   *        threshold, returns null. Use 0.0 to skip validation.
   */
  public AlignToTemplate(String templateImage, double minSimilarityThreshold) {
    this.template = ImageUtil.readFromResource(templateImage);
    this.minSimilarityThreshold = minSimilarityThreshold;
  }

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    return ImageUtil.alignToReferenceWithValidation(image, template, minSimilarityThreshold);
  }
}
