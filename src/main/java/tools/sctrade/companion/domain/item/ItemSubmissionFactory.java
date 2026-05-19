package tools.sctrade.companion.domain.item;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.StatusTrackingSubmissionFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.screenshot.ScreenshotRepository;
import tools.sctrade.companion.domain.screenshot.ScreenshotType;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.NoListingsException;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Factory for building item submissions from screen captures.
 */
public class ItemSubmissionFactory extends StatusTrackingSubmissionFactory<ItemSubmission> {

  private final Logger logger = LoggerFactory.getLogger(ItemSubmissionFactory.class);

  private UserService userService;
  private NotificationService notificationService;
  private final ItemLocationReader itemLocationReader;
  private final ItemShopReader itemShopReader;
  private final ItemListingFactory itemListingFactory;
  private Ocr ocr;

  /**
   * Constructor.
   *
   * @param screenshotRepository repository used to track screenshot processing status
   * @param screenshotType kiosk type to associate with tracked screenshots
   * @param userService the user service
   * @param notificationService the notification service
   * @param itemListingFactory the item listing factory
   * @param itemLocationReader the item location reader
   * @param itemShopReader the item shop reader
   * @param ocr the OCR service
   */
  public ItemSubmissionFactory(ScreenshotRepository screenshotRepository,
      ScreenshotType screenshotType, UserService userService,
      NotificationService notificationService, ItemListingFactory itemListingFactory,
      ItemLocationReader itemLocationReader, ItemShopReader itemShopReader, Ocr ocr) {
    super(screenshotRepository, screenshotType);
    this.userService = userService;
    this.notificationService = notificationService;
    this.itemListingFactory = itemListingFactory;
    this.itemLocationReader = itemLocationReader;
    this.itemShopReader = itemShopReader;
    this.ocr = ocr;
  }

  @Override
  protected ItemSubmission doBuild(BufferedImage screenCapture) {
    var ocrResult = ocr.read(screenCapture);
    var location = readLocation(screenCapture, ocrResult);
    var shop = readShop(screenCapture, ocrResult);

    if (location.isEmpty() || shop.isEmpty()) {
      return new ItemSubmission(userService.get(), List.of());
    }

    var listings = itemListingFactory.build(ocrResult, location.orElse(null), shop.orElse(null));

    if (listings.isEmpty()) {
      throw new NoListingsException();
    }

    return new ItemSubmission(userService.get(), listings);
  }

  @Override
  protected String extractLocation(ItemSubmission result) {
    return result.getListings().stream().map(ItemListing::location).filter(l -> l != null)
        .findFirst().orElse(null);
  }

  @Override
  protected String extractContent(ItemSubmission result) {
    return result.getListings().size() + " listings processed";
  }

  private Optional<String> readLocation(BufferedImage screenCapture, OcrResult ocrResult) {
    var location = itemLocationReader.read(screenCapture, ocrResult);

    if (location.isEmpty()) {
      notificationService.error(LocalizationUtil.get("warnNoLocation"));
    }

    return location;
  }

  private Optional<String> readShop(BufferedImage screenCapture, OcrResult ocrResult) {
    var shop = itemShopReader.read(screenCapture, ocrResult);

    if (shop.isEmpty()) {
      notificationService.error(LocalizationUtil.get("warnNoShop"));
    }

    return shop;
  }
}
