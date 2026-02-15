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
  private static final String TEMPLATE_PATH = "/images/middle_template.jpg";
  private final BufferedImage template;

  /**
   * Creates a new AlignToTemplate manipulation with the default template.
   *
   * @throws ImageProcessingException if the template cannot be loaded.
   */
  public AlignToTemplate() {
    this.template = loadTemplate(TEMPLATE_PATH);
  }

  /**
   * Creates a new AlignToTemplate manipulation with a custom template.
   *
   * @param templateImage The template image to align to.
   */
  public AlignToTemplate(BufferedImage templateImage) {
    this.template = templateImage;
  }

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    return ImageUtil.alignToReference(image, template);
  }

  private BufferedImage loadTemplate(String resourcePath) {
    try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
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
