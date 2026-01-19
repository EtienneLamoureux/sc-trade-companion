package tools.sctrade.companion.domain.commodity;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.manipulations.ConvertToEqualizedGreyscale;
import tools.sctrade.companion.domain.image.manipulations.InvertColors;
import tools.sctrade.companion.domain.image.manipulations.UpscaleTo4k;
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

@Disabled("Shouldn't run during CI/CD. Comment when iterating on the OCR.")
@ExtendWith(MockitoExtension.class)
class CommoditySubmissionFactoryITest {
  private static final double CURRENT_ACCURACY = 35.0;

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
    List<ImageManipulation> imageManipulations =
        List.of(new UpscaleTo4k(), new InvertColors(), new ConvertToEqualizedGreyscale());
    ocr = new WindowsOcr(imageManipulations, diskImageWriter, processRunner,
        new NotificationService(new ConsoleNotificationRepository()));

    submissionFactory = new CommoditySubmissionFactory(userService, notificationService,
        commodityLocationReader, commodityListingFactory, ocr);
  }

  @Test
  void givenTestCasesByColorPaletteWhenProcessingThenCalculateOverallAccuracyScore()
      throws IOException {
    var testCasesByColorPalette = Map.of("uee blue",
        List.of("arc-l1-sell-1", "arc-l2-sell-1", "arc-l3-buy-1", "pyro-gateway-sell-1",
            "seraphim-station-buy-1", "seraphim-station-sell-1"),
        "pyro orange", List.of("canard-view-buy-1", "canard-view-sell-1", "checkmate-buy-1"),
        "levski grey", List.of("levski-buy-1", "levski-buy-2", "levski-sell-1"));

    var scores = new ArrayList<Double>();

    for (var colorPalette : testCasesByColorPalette.keySet()) {
      double colorPaletteScore = 0.0;

      for (var testCase : testCasesByColorPalette.get(colorPalette)) {
        var score = calulateScore(testCase);
        scores.add(score);
        colorPaletteScore += score;
      }

      logger.info("{} (score)\t{}%", colorPalette,
          colorPaletteScore / testCasesByColorPalette.get(colorPalette).size());
    }

    double totalScore = scores.stream().mapToDouble(Double::doubleValue).sum() / scores.size();
    logger.info("TOTAL (score)\t{}%", totalScore);
    assertTrue(totalScore >= CURRENT_ACCURACY);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"arc-l1-sell-1", "arc-l2-sell-1", "arc-l3-buy-1", "pyro-gateway-sell-1",
      "seraphim-station-buy-1", "seraphim-station-sell-1", "canard-view-buy-1",
      "canard-view-sell-1", "checkmate-buy-1", "levski-buy-1", "levski-buy-2", "levski-sell-1"})
  void givenTestCasesWhenProcessingThenCalculateAccuracyScore(String testCase) throws IOException {
    calulateScore(testCase);
  }

  private double calulateScore(String testCase) throws IOException {
    var image = ResourceUtil.getBufferedImage("/kiosks/commodity/images/" + testCase + ".jpg");
    var actualListings = getActualListingsNoFail(image);
    var expectedListings = getExpectedListingsFor(testCase);
    var actualListingsIterator = actualListings.iterator();

    double points = 0.0;
    double total = 0.0;

    for (var expectedListing : expectedListings) {
      total += 6;

      if (actualListingsIterator.hasNext() == false) {
        logger.debug("{} (missing listing)\t{}", testCase, expectedListing.commodity());
        continue;
      }

      var actualListing = actualListingsIterator.next();

      if (expectedListing.location().equalsIgnoreCase(actualListing.location())) {
        points++;
      } else {
        logger.debug("{} (location)\t{} != {}", testCase, expectedListing.location(),
            actualListing.location());
      }

      if (expectedListing.transactionType().equals(actualListing.transactionType())) {
        points++;
      } else {
        logger.debug("{} (transactionType)\t{} != {}", testCase, expectedListing.transactionType(),
            actualListing.transactionType());
      }

      if (expectedListing.commodity().equalsIgnoreCase(actualListing.commodity())) {
        points++;
      } else {
        logger.debug("{} (commodity)\t\t{} != {}", testCase, expectedListing.commodity(),
            actualListing.commodity());
      }

      if (Math.abs(
          expectedListing.price() - actualListing.price()) <= (0.05 * expectedListing.price())) {
        points++;
      } else {
        logger.debug("{} (price)\t\t{} != {}", testCase, expectedListing.price(),
            actualListing.price());
      }

      if (Math.abs(expectedListing.inventory() - actualListing.inventory()) <= (0.001
          * expectedListing.inventory())) {
        points++;
      } else {
        logger.debug("{} (inventory)\t\t{} != {}", testCase, expectedListing.inventory(),
            actualListing.inventory());
      }

      if (expectedListing.boxSizesInScu().equals(actualListing.boxSizesInScu())) {
        points++;
      } else {
        logger.debug("{} (boxSizesInScu)\t{} != {}", testCase, expectedListing.boxSizesInScu(),
            actualListing.boxSizesInScu());
      }
    }

    double score = (points / total) * 100;
    logger.info("{} (score)\t\t{}%", testCase, score);

    return score;
  }

  private Collection<CommodityListing> getActualListingsNoFail(BufferedImage image) {
    try {
      return submissionFactory.build(image).getListings();
    } catch (Exception e) {
      return List.of();
    }
  }

  private List<CommodityListing> getExpectedListingsFor(String testCase) {
    var json = ResourceUtil.getTextLines("/kiosks/commodity/expected/" + testCase + ".json")
        .stream().collect(Collectors.joining(""));
    var expected = JsonUtil.parseList(json, CommodityListing.class);

    return expected;
  }

  private void setupMocks() {
    // when(settings.get(Setting.OUTPUT_TRANSIENT_IMAGES)).thenReturn(true);
    when(settings.get(Setting.OUTPUT_SCREENSHOTS)).thenReturn(true);
    when(settings.get(Setting.MY_IMAGES_PATH))
        .thenReturn(Paths.get(".", "test-images").normalize().toAbsolutePath());
  }
}
