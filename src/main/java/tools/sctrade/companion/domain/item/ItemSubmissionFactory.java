package tools.sctrade.companion.domain.item;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.NoListingsException;
import tools.sctrade.companion.utils.LocalizationUtil;

public class ItemSubmissionFactory {
  private final Logger logger = LoggerFactory.getLogger(ItemSubmissionFactory.class);

  private UserService userService;
  private NotificationService notificationService;
  private final ItemLocationReader itemLocationReader;
  private final ItemShopReader itemShopReader;
  private final ItemListingFactory itemListingFactory;
  private Ocr ocr;

  public ItemSubmissionFactory(UserService userService, NotificationService notificationService,
      ItemListingFactory itemListingFactory, ItemLocationReader itemLocationReader,
      ItemShopReader itemShopReader, Ocr ocr) {
    this.userService = userService;
    this.notificationService = notificationService;
    this.itemListingFactory = itemListingFactory;
    this.itemLocationReader = itemLocationReader;
    this.itemShopReader = itemShopReader;
    this.ocr = ocr;
  }

  public ItemSubmission build(BufferedImage screenCapture) {
    var ocrResult = ocr.read(screenCapture);
    var location = extractLocation(screenCapture, ocrResult);
    var shop = extractShop(screenCapture, ocrResult);

    if (location.isEmpty() || shop.isEmpty()) {
      return new ItemSubmission(userService.get(), List.of());
    }

    var listings = itemListingFactory.build(ocrResult, location.orElse(null), shop.orElse(null));

    if (listings.isEmpty()) {
      throw new NoListingsException();
    }

    return new ItemSubmission(userService.get(), listings);
  }

  private Optional<String> extractLocation(BufferedImage screenCapture, OcrResult ocrResult) {
    var location = itemLocationReader.read(screenCapture, ocrResult);

    if (location.isEmpty()) {
      notificationService.error(LocalizationUtil.get("warnNoLocation"));
    }

    return location;
  }

  private Optional<String> extractShop(BufferedImage screenCapture, OcrResult ocrResult) {
    var shop = itemShopReader.read(screenCapture, ocrResult);

    if (shop.isEmpty()) {
      notificationService.error(LocalizationUtil.get("warnNoShop"));
    }

    return shop;
  }
}
