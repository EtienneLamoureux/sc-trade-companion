package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.manipulations.UpscaleTo4k;
import tools.sctrade.companion.utils.ImageUtil;

public class CommoditySubmissionFactoryAccuracyTest {
  private static final String PREPROCESSED_FOLDER_PATH = "tests/preprocessed";

  private CommoditySubmissionFactory factory;

  @BeforeEach
  void setUp() {
    List<ImageManipulation> preprocessingManipulations = new ArrayList<>();
    // preprocessingManipulations.add(new ConvertToGreyscale());
    // preprocessingManipulations.add(new InvertColors());
    // preprocessingManipulations.add(new AdjustBrightnessAndContrast(10.0f, 0.0f));
    // preprocessingManipulations.add(new WriteToDisk(PREPROCESSED_FOLDER_PATH));

    factory = new CommoditySubmissionFactory(null,
        new CommodityListingsTesseractOcr(preprocessingManipulations));
  }

  @Test
  void givenCorrectSreenshotThenReadTextAccurately() throws IOException {
    var manipulation = new UpscaleTo4k();
    BufferedImage screenshot =
        ImageUtil.getFromResourcePath("/images/kiosks/commodity/2023-10-31_9-27-0-224873600.jpg");
    screenshot = manipulation.manipulate(screenshot);

    factory.build(screenshot);
  }
}
