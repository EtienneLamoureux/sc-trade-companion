package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.image.manipulations.AdjustBrightnessAndContrast;
import tools.sctrade.companion.domain.image.manipulations.ConvertToGreyscale;
import tools.sctrade.companion.domain.image.manipulations.InvertColors;
import tools.sctrade.companion.domain.image.manipulations.UpscaleTo4k;
import tools.sctrade.companion.domain.image.manipulations.WriteToDisk;
import tools.sctrade.companion.domain.user.Setting;
import tools.sctrade.companion.domain.user.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.utils.ImageUtil;

class CommoditySubmissionFactoryAccuracyTest {
  private SettingRepository settings;
  private ImageWriter imageWriter;
  private CommoditySubmissionFactory factory;

  @BeforeEach
  void setUp() {
    initializeSettings();
    imageWriter = new DiskImageWriter(settings);

    List<ImageManipulation> preprocessingManipulations = new ArrayList<>();
    preprocessingManipulations.add(new ConvertToGreyscale());
    preprocessingManipulations.add(new InvertColors());
    preprocessingManipulations.add(new AdjustBrightnessAndContrast(10.0f, 0.0f));
    preprocessingManipulations.add(new WriteToDisk(imageWriter));

    factory = new CommoditySubmissionFactory(new UserService(),
        new CommodityListingsTesseractOcr(preprocessingManipulations),
        new CommodityLocationTesseractOcr(preprocessingManipulations), imageWriter);
  }

  @Test
  void givenCorrectSreenshotThenReadTextAccurately() throws IOException {
    var manipulation = new UpscaleTo4k();
    BufferedImage screenshot = ImageUtil
        .getFromResourcePath("/images/kiosks/commodity/ScreenShot-2023-10-27_15-28-07-12F.jpg");
    screenshot = manipulation.manipulate(screenshot);

    var submission = factory.build(screenshot);
    System.out.println(submission.toString());
  }

  private void initializeSettings() {
    settings = new SettingRepository();
    settings.set(Setting.MY_IMAGES_PATH, Paths.get(".", "my-images").toAbsolutePath());
    settings.set(Setting.OUTPUT_TRANSIENT_IMAGES, false);
  }
}