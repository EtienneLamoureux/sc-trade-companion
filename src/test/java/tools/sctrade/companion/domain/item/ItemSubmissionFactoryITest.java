package tools.sctrade.companion.domain.item;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.TestLocationRepository;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.notification.ConsoleNotificationRepository;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OneOcr;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.utils.JsonUtil;
import tools.sctrade.companion.utils.ResourceUtil;

// @Disabled("Shouldn't run during CI/CD. Comment when iterating on the OCR.")
@ExtendWith(MockitoExtension.class)
class ItemSubmissionFactoryITest {
  private static final double CURRENT_ACCURACY = 0.0;

  private final Logger logger = LoggerFactory.getLogger(ItemSubmissionFactoryITest.class);

  @Mock
  private UserService userService;
  @Mock
  private SettingRepository settings;

  private DiskImageWriter diskImageWriter;

  private LocationRepository locationRepository = new TestLocationRepository();

  private ItemLocationReader itemLocationReader = new ItemLocationReader(locationRepository);
  private ItemShopReader itemShopReader = new ItemShopReader();
  private ItemListingFactory itemListingFactory = new ItemListingFactory(new TestItemRepository());
  private Ocr ocr;
  private NotificationService notificationService =
      new NotificationService(new ConsoleNotificationRepository());

  private ItemSubmissionFactory submissionFactory;

  @BeforeEach
  void setUp() {
    setupMocks();

    diskImageWriter = new DiskImageWriter(settings);
    List<ImageManipulation> imageManipulations = List.of();
    ocr = new OneOcr(imageManipulations, diskImageWriter);

    submissionFactory = new ItemSubmissionFactory(userService, notificationService,
        itemListingFactory, itemLocationReader, itemShopReader, ocr);
  }

  @Test
  void givenTestCasesByShopWhenProcessingThenCalculateOverallAccuracyScore() throws IOException {
    var testCasesByShop = new java.util.LinkedHashMap<String, List<String>>();
    testCasesByShop.put("armor", List.of("armor-1", "armor-2", "armor-3"));
    testCasesByShop.put("casaba-outlet", List.of("casaba-outlet-1"));
    testCasesByShop.put("live-fire-weapons",
        List.of("live-fire-weapons-1", "live-fire-weapons-2", "live-fire-weapons-3"));
    testCasesByShop.put("medical_shop",
        List.of("medical_shop-1", "medical_shop-2", "medical_shop-3"));
    testCasesByShop.put("platinum-bay",
        List.of("platinum-bay-1", "platinum-bay-2", "platinum-bay-3"));
    testCasesByShop.put("ship-weapons", List.of("ship-weapons-1"));
    testCasesByShop.put("shop_terminal",
        List.of("shop_terminal-1", "shop_terminal-2", "shop_terminal-3"));
    testCasesByShop.put("weapons_shop",
        List.of("weapons_shop-1", "weapons_shop-2", "weapons_shop-3"));

    var scores = new ArrayList<Double>();

    for (var shop : testCasesByShop.keySet()) {
      double shopScore = 0.0;

      for (var testCase : testCasesByShop.get(shop)) {
        var score = calulateScore(testCase);
        scores.add(score);
        shopScore += score;
      }

      logger.info("{} (score)\t{}%", shop, shopScore / testCasesByShop.get(shop).size());
    }

    double totalScore = scores.stream().mapToDouble(Double::doubleValue).sum() / scores.size();
    logger.info("TOTAL (score)\t{}%", totalScore);
    assertTrue(totalScore >= CURRENT_ACCURACY);
  }

  @ParameterizedTest(name = "{0}")
  // @ValueSource(strings = {"casaba-outlet-1"})
  @ValueSource(strings = {"armor-1", "armor-2", "armor-3", "casaba-outlet-1", "live-fire-weapons-1",
      "live-fire-weapons-2", "live-fire-weapons-3", "medical_shop-1", "medical_shop-2",
      "medical_shop-3", "platinum-bay-1", "platinum-bay-2", "platinum-bay-3", "ship-weapons-1",
      "shop_terminal-1", "shop_terminal-2", "shop_terminal-3", "weapons_shop-1", "weapons_shop-2",
      "weapons_shop-3"})
  void givenTestCasesWhenProcessingThenCalculateAccuracyScore(String testCase) throws IOException {
    calulateScore(testCase);
  }

  private double calulateScore(String testCase) throws IOException {
    var image = ResourceUtil.getBufferedImage("/kiosks/item/images/" + testCase + ".jpg");
    var actualListings = getActualListingsNoFail(image);
    var expectedListings = getExpectedListingsFor(testCase);
    var actualListingsIterator = actualListings.iterator();

    double points = 0.0;
    double total = 0.0;

    for (var expectedListing : expectedListings) {
      total += 4;

      if (!actualListingsIterator.hasNext()) {
        logger.debug("{} (missing listing)\t{}", testCase, expectedListing.name());
        continue;
      }

      var actualListing = actualListingsIterator.next();

      if (expectedListing.location().equalsIgnoreCase(actualListing.location())) {
        points++;
      } else {
        logger.debug("{} (location)\t{} != {}", testCase, expectedListing.location(),
            actualListing.location());
      }

      if (expectedListing.shop().equalsIgnoreCase(actualListing.shop())) {
        points++;
      } else {
        logger.debug("{} (shop)\t\t{} != {}", testCase, expectedListing.shop(),
            actualListing.shop());
      }

      if (expectedListing.name().equalsIgnoreCase(actualListing.name())) {
        points++;
      } else {
        logger.debug("{} (name)\t\t{} != {}", testCase, expectedListing.name(),
            actualListing.name());
      }

      if (Math.abs(
          expectedListing.price() - actualListing.price()) <= (0.05 * expectedListing.price())) {
        points++;
      } else {
        logger.debug("{} (price)\t\t{} != {}", testCase, expectedListing.price(),
            actualListing.price());
      }
    }

    double score = total == 0.0 ? 0.0 : (points / total) * 100;
    logger.info("{} (score)\t{}%", testCase, score);

    return score;
  }

  private List<ItemListing> getExpectedListingsFor(String testCase) {
    var json = ResourceUtil.getTextLines("/kiosks/item/expected/" + testCase + ".json").stream()
        .collect(Collectors.joining(""));

    return JsonUtil.parseList(json, ItemListing.class);
  }

  private Collection<ItemListing> getActualListingsNoFail(BufferedImage image) {
    try {
      return submissionFactory.build(image).getListings();
    } catch (Exception e) {
      return List.of();
    }
  }

  private void setupMocks() {
    when(settings.get(Setting.OUTPUT_SCREENSHOTS)).thenReturn(true);
    when(settings.get(Setting.MY_IMAGES_PATH))
        .thenReturn(Paths.get(".", "test-images").normalize().toAbsolutePath());
  }
}
