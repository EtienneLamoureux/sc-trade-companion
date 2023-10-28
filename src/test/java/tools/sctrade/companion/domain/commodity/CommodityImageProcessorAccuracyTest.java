package tools.sctrade.companion.domain.commodity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.manipulations.AdjustBrightnessAndContrast;
import tools.sctrade.companion.domain.image.manipulations.ConvertToGreyscale;
import tools.sctrade.companion.domain.image.manipulations.InvertColors;
import tools.sctrade.companion.domain.image.manipulations.WriteToDisk;
import tools.sctrade.companion.utils.ImageUtil;

public class CommodityImageProcessorAccuracyTest {
  private static final String PREPROCESSED_FOLDER_PATH = "tests/preprocessed";

  private CommodityService service;

  @BeforeEach
  void setUp() {
    List<ImageManipulation> preprocessingManipulations = new ArrayList<>();
    preprocessingManipulations.add(new ConvertToGreyscale());
    preprocessingManipulations.add(new InvertColors());
    preprocessingManipulations.add(new AdjustBrightnessAndContrast(10.0f, 0.0f));
    preprocessingManipulations.add(new WriteToDisk(PREPROCESSED_FOLDER_PATH));

    service = new CommodityService(null, new ListingsTesseractOcr(preprocessingManipulations),
        Collections.emptyList());
  }

  @Test
  void givenCorrectSreenshotThenReadTextAccurately() throws IOException {
    service.process(ImageUtil
        .getFromResourcePath("/images/kiosks/commodity/ScreenShot-2023-09-26_19-21-06-5BE.jpg"));
  }
}
