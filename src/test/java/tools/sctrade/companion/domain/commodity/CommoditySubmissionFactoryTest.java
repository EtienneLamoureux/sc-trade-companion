package tools.sctrade.companion.domain.commodity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.notification.ConsoleNotificationRepository;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.screenshot.ScreenshotRepository;
import tools.sctrade.companion.domain.screenshot.ScreenshotStatus;
import tools.sctrade.companion.domain.screenshot.ScreenshotType;
import tools.sctrade.companion.domain.user.User;
import tools.sctrade.companion.domain.user.UserService;

@ExtendWith(MockitoExtension.class)
class CommoditySubmissionFactoryTest {

  @Mock
  private UserService userService;
  @Mock
  private Ocr ocr;
  @Mock
  private CommodityLocationReader commodityLocationReader;
  @Mock
  private CommodityListingFactory commodityListingFactory;

  private ScreenshotRepository screenshotRepository;
  private NotificationService notificationService;
  private CommoditySubmissionFactory factory;
  private BufferedImage image;

  private static final User USER = new User("id", "label");
  private static final CommodityListing TEST_LISTING = new CommodityListing("Area18",
      TransactionType.SELLS, "Aluminum", List.of(1), "batch", Instant.now());

  @BeforeEach
  void setUp() {
    screenshotRepository = new ScreenshotRepository();
    notificationService = new NotificationService(new ConsoleNotificationRepository());
    image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

    factory = new CommoditySubmissionFactory(screenshotRepository, ScreenshotType.COMMODITY_KIOSK,
        userService, notificationService, commodityLocationReader, commodityListingFactory, ocr);
  }

  @Test
  void whenBuildSucceeds_thenRepositoryHasSuccessStatus() {
    when(ocr.read(any())).thenReturn(new OcrResult(List.of()));
    when(commodityLocationReader.read(any(BufferedImage.class), any(OcrResult.class)))
        .thenReturn(Optional.empty());
    when(commodityListingFactory.build(any(OcrResult.class), any()))
        .thenReturn(List.of(TEST_LISTING));
    when(userService.get()).thenReturn(USER);

    factory.build(image);

    assertEquals(ScreenshotStatus.SUCCESS, screenshotRepository.getSnapshot().get(0).status());
  }

  @Test
  void whenBuildThrows_thenRepositoryHasErrorStatus() {
    when(ocr.read(any())).thenThrow(new RuntimeException("ocr failure"));

    try {
      factory.build(image);
    } catch (RuntimeException e) {
      // expected
    }

    assertEquals(ScreenshotStatus.ERROR, screenshotRepository.getSnapshot().get(0).status());
  }

  @Test
  void givenSubmissionWithListings_whenExtractingLocation_thenReturnsFirstListingLocation() {
    var listing = new CommodityListing("Orison", TransactionType.SELLS, "Gold", List.of(1), "batch",
        Instant.now());
    var submission = new CommoditySubmission(USER, List.of(listing));
    var testFactory = new ExposedCommoditySubmissionFactory(screenshotRepository,
        ScreenshotType.COMMODITY_KIOSK, userService, notificationService, commodityLocationReader,
        commodityListingFactory, ocr);

    assertEquals("Orison", testFactory.publicExtractLocation(submission));
  }

  @Test
  void givenSubmissionWithNoLocatedListings_whenExtractingLocation_thenReturnsNull() {
    var listing = new CommodityListing(null, TransactionType.SELLS, "Gold", List.of(1), "batch",
        Instant.now());
    var submission = new CommoditySubmission(USER, List.of(listing));
    var testFactory = new ExposedCommoditySubmissionFactory(screenshotRepository,
        ScreenshotType.COMMODITY_KIOSK, userService, notificationService, commodityLocationReader,
        commodityListingFactory, ocr);

    assertNull(testFactory.publicExtractLocation(submission));
  }

  @Test
  void givenSubmissionWithListings_whenExtractingContent_thenReturnsSummaryWithListingCount() {
    var listing1 = new CommodityListing("Area18", TransactionType.SELLS, "Aluminum", List.of(1),
        "batch", Instant.now());
    var listing2 = new CommodityListing("Area18", TransactionType.BUYS, "Titanium", List.of(1),
        "batch", Instant.now());
    var submission = new CommoditySubmission(USER, List.of(listing1, listing2));
    var testFactory = new ExposedCommoditySubmissionFactory(screenshotRepository,
        ScreenshotType.COMMODITY_KIOSK, userService, notificationService, commodityLocationReader,
        commodityListingFactory, ocr);

    assertTrue(testFactory.publicExtractContent(submission).contains("2"));
  }

  static class ExposedCommoditySubmissionFactory extends CommoditySubmissionFactory {

    ExposedCommoditySubmissionFactory(ScreenshotRepository screenshotRepository,
        ScreenshotType screenshotType, UserService userService,
        NotificationService notificationService, CommodityLocationReader commodityLocationReader,
        CommodityListingFactory commodityListingFactory, Ocr ocr) {
      super(screenshotRepository, screenshotType, userService, notificationService,
          commodityLocationReader, commodityListingFactory, ocr);
    }

    String publicExtractLocation(CommoditySubmission submission) {
      return extractLocation(submission);
    }

    String publicExtractContent(CommoditySubmission submission) {
      return extractContent(submission);
    }
  }
}
