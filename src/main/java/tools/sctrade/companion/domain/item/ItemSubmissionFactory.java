package tools.sctrade.companion.domain.item;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.exceptions.NoListingsException;
import tools.sctrade.companion.utils.LocalizationUtil;

public class ItemSubmissionFactory {
  private static final String WALLET = "wallet:";
  private static final String CHOOSE_CATEGORY = "choose category";

  private final Logger logger = LoggerFactory.getLogger(ItemSubmissionFactory.class);

  private UserService userService;
  private NotificationService notificationService;
  private final ItemLocationReader itemLocationReader;
  private final ItemListingFactory itemListingFactory;
  private Ocr ocr;

  public ItemSubmissionFactory(UserService userService, NotificationService notificationService,
      ItemListingFactory itemListingFactory, ItemLocationReader itemLocationReader, Ocr ocr) {
    this.userService = userService;
    this.notificationService = notificationService;
    this.itemListingFactory = itemListingFactory;
    this.itemLocationReader = itemLocationReader;
    this.ocr = ocr;
  }

  public ItemSubmission build(BufferedImage screenCapture) {
    var ocrResult = ocr.read(screenCapture);
    Rectangle topLeftBoundingBox =
        new Rectangle(0, 0, (screenCapture.getWidth() / 2), (screenCapture.getHeight() / 3));
    var topLeftCornerOcrResult = ocrResult.crop(topLeftBoundingBox);
    var location = itemLocationReader.read(topLeftCornerOcrResult);

    if (location.isEmpty()) {
      notificationService.warn(LocalizationUtil.get("warnNoLocation"));
    }

    Rectangle listingsBoundingBox = calculateListingsBoundingBox(ocrResult);
    var listingsOcrResult = ocrResult.crop(listingsBoundingBox);
    var listings = itemListingFactory.build(listingsOcrResult, location.orElse(null));

    if (listings.isEmpty()) {
      throw new NoListingsException();
    }

    return new ItemSubmission(userService.get(), listings);
  }

  private Rectangle calculateListingsBoundingBox(OcrResult ocrResult) {
    int minX;
    int maxX;
    int minY;
    int maxY = (int) ocrResult.getBoundingBox().getMaxY();

    try {
      var chooseCategoryFragment = OcrUtil.findFragmentClosestTo(ocrResult, CHOOSE_CATEGORY);
      var categoryBox = chooseCategoryFragment.getBoundingBox();
      minX = (int) categoryBox.getMinX();
      minY = (int) (categoryBox.getMaxY() + 2.5 * categoryBox.getHeight());
    } catch (NoCloseStringException e) {
      logger.warn("Could not find '{}' fragment. Falling back to default listing bounds",
          CHOOSE_CATEGORY);
      minX = (int) ocrResult.getBoundingBox().getMinX();
      minY = (int) ocrResult.getBoundingBox().getMinY();
    }

    try {
      var walletFragment = ocrResult.getFragments().parallelStream()
          .filter(n -> n.getText().trim().startsWith(WALLET)).findFirst()
          .orElseThrow(() -> new NoCloseStringException(WALLET));
      maxX = (int) walletFragment.getBoundingBox().getMinX();
    } catch (NoCloseStringException e) {
      logger.warn("Could not find '{}' fragment. Falling back to default listing bounds", WALLET);
      maxX = (int) ocrResult.getBoundingBox().getMaxX();
    }

    int width = maxX - minX;
    int height = maxY - minY;

    return new Rectangle(minX, minY, width, height);
  }
}
