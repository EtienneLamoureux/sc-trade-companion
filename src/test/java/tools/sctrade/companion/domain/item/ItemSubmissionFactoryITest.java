package tools.sctrade.companion.domain.item;

import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
import tools.sctrade.companion.utils.ResourceUtil;

// @Disabled("Shouldn't run during CI/CD. Comment when iterating on the OCR.")
@ExtendWith(MockitoExtension.class)
class ItemSubmissionFactoryITest {
  private final Logger logger = LoggerFactory.getLogger(ItemSubmissionFactoryITest.class);

  @Mock
  private UserService userService;
  @Mock
  private SettingRepository settings;

  private DiskImageWriter diskImageWriter;

  private LocationRepository locationRepository = new TestLocationRepository();

  private ItemLocationReader itemLocationReader = new ItemLocationReader(locationRepository);
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
        itemListingFactory, itemLocationReader, ocr);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"armor-1"})
  // @ValueSource(strings = {"arc-l1-sell-1", "arc-l2-sell-1", "arc-l3-buy-1",
  // "pyro-gateway-sell-1",
  // "seraphim-station-buy-1", "seraphim-station-sell-1", "canard-view-buy-1",
  // "canard-view-sell-1", "checkmate-buy-1", "levski-buy-1", "levski-buy-2", "levski-sell-1",
  // "rayari-anvik-buy-1", "lorville-sell-1"})
  void givenTestCasesWhenProcessingThenCalculateAccuracyScore(String testCase) throws IOException {
    calulateScore(testCase);
  }

  private double calulateScore(String testCase) throws IOException {
    var image = ResourceUtil.getBufferedImage("/kiosks/item/images/" + testCase + ".jpg");
    var actualListings = getActualListingsNoFail(image);
    // var expectedListings = getExpectedListingsFor(testCase);
    var actualListingsIterator = actualListings.iterator();

    double points = 0.0;
    double total = 0.0;

    return 0.0;
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
