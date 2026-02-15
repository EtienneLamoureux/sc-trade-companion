package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.exceptions.ImageProcessingException;
import tools.sctrade.companion.utils.ImageUtil;

/**
 * Image manipulation that performs perspective correction on Star Citizen kiosk screens. This
 * manipulation detects the kiosk bezel and corrects the perspective to produce a front-facing,
 * rectangular view of the screen.
 */
public class StarCitizenKioskPerspectiveCorrection implements ImageManipulation {
  private static final Logger logger =
      LoggerFactory.getLogger(StarCitizenKioskPerspectiveCorrection.class);
  private final StarCitizenKioskCorrector corrector;

  /**
   * Creates a new instance of StarCitizenKioskPerspectiveCorrection.
   */
  public StarCitizenKioskPerspectiveCorrection() {
    this.corrector = new StarCitizenKioskCorrector();
  }

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    try {
      // Convert BufferedImage to Mat
      Mat inputMat = ImageUtil.toMat(image);

      // Preprocess and detect kiosk
      Mat edges = corrector.preprocess(inputMat);
      var contour = corrector.detectKioskContour(edges, inputMat);
      var corners = corrector.orderCorners(contour);

      // Warp perspective
      Mat resultMat = corrector.warpPerspective(inputMat, corners);

      // Convert back to BufferedImage
      BufferedImage result = ImageUtil.toBufferedImage(resultMat);

      // Release resources
      edges.release();
      contour.release();
      inputMat.release();
      resultMat.release();

      return result;
    } catch (IOException e) {
      logger.error("Error during kiosk perspective correction", e);
      throw new ImageProcessingException(e);
    } catch (RuntimeException e) {
      logger.error("Failed to detect kiosk in image: {}", e.getMessage());
      throw new ImageProcessingException(e);
    }
  }
}
