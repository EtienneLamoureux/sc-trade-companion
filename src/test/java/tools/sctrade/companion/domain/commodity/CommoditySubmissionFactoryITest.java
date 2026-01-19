package tools.sctrade.companion.domain.commodity;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.manipulations.ConvertToEqualizedGreyscale;
import tools.sctrade.companion.domain.image.manipulations.InvertColors;
import tools.sctrade.companion.domain.image.manipulations.UpscaleTo4k;
import tools.sctrade.companion.domain.image.manipulations.WriteToDisk;
import tools.sctrade.companion.domain.notification.ConsoleNotificationRepository;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.WindowsOcr;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.utils.JsonUtil;
import tools.sctrade.companion.utils.ProcessRunner;
import tools.sctrade.companion.utils.ResourceUtil;

@ExtendWith(MockitoExtension.class)
class CommoditySubmissionFactoryITest {
  private final Logger logger = LoggerFactory.getLogger(CommoditySubmissionFactoryITest.class);

  @Mock
  private UserService userService;
  @Mock
  private SettingRepository settings;

  private DiskImageWriter diskImageWriter;
  private ProcessRunner processRunner = new ProcessRunner();

  private LocationRepository locationRepository = new TestLocationRepository();
  private CommodityRepository commodityRepository = new TestCommodityRepository();

  private CommodityLocationReader commodityLocationReader =
      new CommodityLocationReader(locationRepository);
  private CommodityListingFactory commodityListingFactory =
      new CommodityListingFactory(commodityRepository);
  private Ocr ocr;
  private NotificationService notificationService =
      new NotificationService(new ConsoleNotificationRepository());

  private CommoditySubmissionFactory submissionFactory;

  @BeforeEach
  void setUp() {
    setupMocks();

    diskImageWriter = new DiskImageWriter(settings);
    List<ImageManipulation> imageManipulations = List.of(new UpscaleTo4k(),
        new WriteToDisk(diskImageWriter), new InvertColors(), new ConvertToEqualizedGreyscale());
    ocr = new WindowsOcr(imageManipulations, diskImageWriter, processRunner,
        new NotificationService(new ConsoleNotificationRepository()));

    submissionFactory = new CommoditySubmissionFactory(userService, notificationService,
        commodityLocationReader, commodityListingFactory, ocr);
  }

  @Test
  void bonjour() throws IOException, URISyntaxException {
    var testCase = "arc-l1-sell-1";

    var image = ResourceUtil.getBufferedImage("/kiosks/commodity/images/" + testCase + ".jpg");
    var actualListings = submissionFactory.build(image).getListings();

    double score = calulateScore(testCase, actualListings);
  }

  private double calulateScore(String testCase, Collection<CommodityListing> actualListings) {
    var expectedListings = getExpectedListingsFor(testCase);
    var actualListingsIterator = actualListings.iterator();

    double points = 0.0;
    double total = 0.0;

    for (var expectedListing : expectedListings) {
      total += 6;

      if (actualListingsIterator.hasNext() == false) {
        continue;
      }

      var actualListing = actualListingsIterator.next();

      if (expectedListing.location().equalsIgnoreCase(actualListing.location())) {
        points++;
      } else {
        logger.debug("{}: {} != {}", testCase, expectedListing.location(),
            actualListing.location());
      }

      if (expectedListing.transactionType().equals(actualListing.transactionType())) {
        points++;
      } else {
        logger.debug("{}: {} != {}", testCase, expectedListing.transactionType(),
            actualListing.transactionType());
      }

      if (expectedListing.commodity().equalsIgnoreCase(actualListing.commodity())) {
        points++;
      } else {
        logger.debug("{}: {} != {}", testCase, expectedListing.commodity(),
            actualListing.commodity());
      }

      if (Math.abs(
          expectedListing.price() - actualListing.price()) <= (0.05 * expectedListing.price())) {
        points++;
      } else {
        logger.debug("{}: {} != {}", testCase, expectedListing.price(), actualListing.price());
      }

      if (expectedListing.inventory() == actualListing.inventory()) {
        points++;
      } else {
        logger.debug("{}: {} != {}", testCase, expectedListing.inventory(),
            actualListing.inventory());
      }

      if (expectedListing.boxSizesInScu().equals(actualListing.boxSizesInScu())) {
        points++;
      } else {
        logger.debug("{}: {} != {}", testCase, expectedListing.boxSizesInScu(),
            actualListing.boxSizesInScu());
      }
    }

    double score = (points / total) * 100;
    logger.debug("{}: {}%", testCase, score);

    return score;
  }

  private List<CommodityListing> getExpectedListingsFor(String testCase) {
    var json = ResourceUtil.getTextLines("/kiosks/commodity/expected/" + testCase + ".json")
        .stream().collect(Collectors.joining(""));
    var expected = JsonUtil.parseList(json, CommodityListing.class);

    return expected;
  }

  private void setupMocks() {
    when(settings.get(Setting.OUTPUT_TRANSIENT_IMAGES)).thenReturn(true);
    when(settings.get(Setting.OUTPUT_SCREENSHOTS)).thenReturn(true);
    when(settings.get(Setting.MY_IMAGES_PATH))
        .thenReturn(Paths.get(".", "test-images").normalize().toAbsolutePath());
  }
}
