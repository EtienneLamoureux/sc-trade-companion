package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.image.manipulations.CommodityKioskTextThreshold1;
import tools.sctrade.companion.domain.image.manipulations.CommodityKioskTextThreshold2;
import tools.sctrade.companion.domain.image.manipulations.CommodityKioskTextThreshold3;
import tools.sctrade.companion.domain.image.manipulations.ConvertToGreyscale;
import tools.sctrade.companion.domain.image.manipulations.InvertColors;
import tools.sctrade.companion.domain.image.manipulations.UpscaleTo4k;
import tools.sctrade.companion.domain.image.manipulations.WriteToDisk;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.user.Setting;
import tools.sctrade.companion.domain.user.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.utils.ImageUtil;

@ExtendWith(MockitoExtension.class)
class CommoditySubmissionFactoryAccuracyTest {
  @Mock
  private NotificationService notificationService;

  private SettingRepository settings;
  private ImageWriter imageWriter;
  private CommoditySubmissionFactory factory;

  @BeforeEach
  void setUp() {
    initializeSettings();
    imageWriter = new DiskImageWriter(settings);

    factory = new CommoditySubmissionFactory(new UserService(settings), notificationService,
        buildBestEffortCommodityLocationReader(), buildBestEffortCommodityListingFactory());
  }

  @Test
  void givenCorrectSreenshotThenReadTextAccurately() throws IOException {
    var manipulation = new UpscaleTo4k();
    BufferedImage screenshot =
        ImageUtil.getFromResourcePath("/images/kiosks/commodity/2023-12-15_21-5-11-930335300.jpg");
    screenshot = manipulation.manipulate(screenshot);

    var submission = factory.build(screenshot);
    System.out.println(submission.toString());
  }

  private void initializeSettings() {
    settings = new SettingRepository();
    settings.set(Setting.MY_IMAGES_PATH, Paths.get(".", "my-images").toAbsolutePath());
    settings.set(Setting.OUTPUT_TRANSIENT_IMAGES, true);
  }

  private BestEffortCommodityLocationReader buildBestEffortCommodityLocationReader() {
    Collection<CommodityLocationReader> locationReaders = new ArrayList<>();
    LocationRepository locationRepository = new TestLocationRepository();

    locationReaders
        .add(new CommodityLocationReader(
            Arrays.asList(new InvertColors(), new ConvertToGreyscale(),
                new CommodityKioskTextThreshold1(), new WriteToDisk(imageWriter)),
            locationRepository));
    locationReaders
        .add(new CommodityLocationReader(
            Arrays.asList(new InvertColors(), new ConvertToGreyscale(),
                new CommodityKioskTextThreshold2(), new WriteToDisk(imageWriter)),
            locationRepository));
    locationReaders
        .add(new CommodityLocationReader(
            Arrays.asList(new InvertColors(), new ConvertToGreyscale(),
                new CommodityKioskTextThreshold3(), new WriteToDisk(imageWriter)),
            locationRepository));

    return new BestEffortCommodityLocationReader(locationReaders);
  }

  private BestEffortCommodityListingFactory buildBestEffortCommodityListingFactory() {
    var commodityRepository = new TestCommodityRepository();

    Collection<CommodityListingFactory> commodityListingFactories = new ArrayList<>();
    commodityListingFactories.add(new CommodityListingFactory(commodityRepository, imageWriter,
        Arrays.asList(new InvertColors(), new ConvertToGreyscale(),
            new CommodityKioskTextThreshold1(), new WriteToDisk(imageWriter))));
    commodityListingFactories.add(new CommodityListingFactory(commodityRepository, imageWriter,
        Arrays.asList(new InvertColors(), new ConvertToGreyscale(),
            new CommodityKioskTextThreshold2(), new WriteToDisk(imageWriter))));
    commodityListingFactories.add(new CommodityListingFactory(commodityRepository, imageWriter,
        Arrays.asList(new InvertColors(), new ConvertToGreyscale(),
            new CommodityKioskTextThreshold3(), new WriteToDisk(imageWriter))));

    return new BestEffortCommodityListingFactory(commodityListingFactories);
  }
}
