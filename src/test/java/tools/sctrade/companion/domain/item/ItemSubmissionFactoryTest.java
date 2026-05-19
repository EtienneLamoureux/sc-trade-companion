package tools.sctrade.companion.domain.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
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
class ItemSubmissionFactoryTest {

  @Mock
  private UserService userService;
  @Mock
  private Ocr ocr;
  @Mock
  private ItemLocationReader itemLocationReader;
  @Mock
  private ItemShopReader itemShopReader;
  @Mock
  private ItemListingFactory itemListingFactory;

  private ScreenshotRepository screenshotRepository;
  private NotificationService notificationService;
  private ItemSubmissionFactory factory;
  private BufferedImage image;

  private static final User USER = new User("id", "label");

  @BeforeEach
  void setUp() {
    screenshotRepository = new ScreenshotRepository();
    notificationService = new NotificationService(new ConsoleNotificationRepository());
    image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

    factory =
        new ItemSubmissionFactory(screenshotRepository, ScreenshotType.ITEM_KIOSK, userService,
            notificationService, itemListingFactory, itemLocationReader, itemShopReader, ocr);
  }

  @Test
  void whenBuildSucceeds_thenRepositoryHasSuccessStatus() {
    when(ocr.read(any())).thenReturn(new OcrResult(List.of()));
    when(itemLocationReader.read(any(BufferedImage.class), any(OcrResult.class)))
        .thenReturn(Optional.empty());
    when(itemShopReader.read(any(BufferedImage.class), any(OcrResult.class)))
        .thenReturn(Optional.empty());
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
    var listing = new ItemListing("item", 100.0, "Area18", "Shop");
    var submission = new ItemSubmission(USER, List.of(listing));
    var testFactory = new ExposedItemSubmissionFactory(screenshotRepository,
        ScreenshotType.ITEM_KIOSK, userService, notificationService, itemListingFactory,
        itemLocationReader, itemShopReader, ocr);

    assertEquals("Area18", testFactory.publicExtractLocation(submission));
  }

  @Test
  void givenEmptySubmission_whenExtractingLocation_thenReturnsNull() {
    var submission = new ItemSubmission(USER, List.of());
    var testFactory = new ExposedItemSubmissionFactory(screenshotRepository,
        ScreenshotType.ITEM_KIOSK, userService, notificationService, itemListingFactory,
        itemLocationReader, itemShopReader, ocr);

    assertNull(testFactory.publicExtractLocation(submission));
  }

  @Test
  void givenSubmissionWithListings_whenExtractingContent_thenReturnsSummaryWithListingCount() {
    var listing1 = new ItemListing("item1", 100.0, "Area18", "Shop");
    var listing2 = new ItemListing("item2", 200.0, "Area18", "Shop");
    var submission = new ItemSubmission(USER, List.of(listing1, listing2));
    var testFactory = new ExposedItemSubmissionFactory(screenshotRepository,
        ScreenshotType.ITEM_KIOSK, userService, notificationService, itemListingFactory,
        itemLocationReader, itemShopReader, ocr);

    assertTrue(testFactory.publicExtractContent(submission).contains("2"));
  }

  static class ExposedItemSubmissionFactory extends ItemSubmissionFactory {

    ExposedItemSubmissionFactory(ScreenshotRepository screenshotRepository,
        ScreenshotType screenshotType, UserService userService,
        NotificationService notificationService, ItemListingFactory itemListingFactory,
        ItemLocationReader itemLocationReader, ItemShopReader itemShopReader, Ocr ocr) {
      super(screenshotRepository, screenshotType, userService, notificationService,
          itemListingFactory, itemLocationReader, itemShopReader, ocr);
    }

    String publicExtractLocation(ItemSubmission submission) {
      return extractLocation(submission);
    }

    String publicExtractContent(ItemSubmission submission) {
      return extractContent(submission);
    }
  }
}
