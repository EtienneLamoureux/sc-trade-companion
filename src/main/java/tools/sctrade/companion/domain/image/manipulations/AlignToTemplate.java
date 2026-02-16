package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.exceptions.ImageProcessingException;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Aligns an image to a reference template using homography transformation. This manipulation uses
 * ORB feature detection on the blue channel to find matching points between the input image and a
 * reference template, then applies a perspective transformation to align the image.
 */
public class AlignToTemplate implements ImageManipulation {
  private static final String TEMPLATE_PATH = "/images/ocr/commodity_kiosk_template.jpg";
  private final BufferedImage template;
  private final double minSimilarityThreshold;

  /**
   * Creates a new AlignToTemplate manipulation with the default template and no validation.
   *
   * @throws ImageProcessingException if the template cannot be loaded.
   */
  public AlignToTemplate() {
    this(loadTemplate(TEMPLATE_PATH), 0.0);
  }

  /**
   * Creates a new AlignToTemplate manipulation with a custom template and no validation.
   *
   * @param templateImage The template image to align to.
   */
  public AlignToTemplate(BufferedImage templateImage) {
    this(templateImage, 0.0);
  }

  /**
   * Creates a new AlignToTemplate manipulation with a custom template and validation threshold.
   *
   * @param templateImage The template image to align to.
   * @param minSimilarityThreshold Minimum similarity score (0.0 to 1.0) for the alignment to be
   *        considered valid. If the aligned image's similarity to the template is below this
   *        threshold, returns null. Use 0.0 to skip validation.
   */
  public AlignToTemplate(BufferedImage templateImage, double minSimilarityThreshold) {
    this.template = templateImage;
    this.minSimilarityThreshold = minSimilarityThreshold;
  }

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    return ImageUtil.alignToReferenceWithValidation(image, template, minSimilarityThreshold);
  }

  private static BufferedImage loadTemplate(String resourcePath) {
    try (InputStream is = AlignToTemplate.class.getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new ImageProcessingException(
            new IOException("Template image not found at " + resourcePath));
      }
      return ImageIO.read(is);
    } catch (IOException e) {
      throw new ImageProcessingException(e);
    }
  }
}
