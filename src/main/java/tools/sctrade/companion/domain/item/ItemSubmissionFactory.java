package tools.sctrade.companion.domain.item;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.exceptions.NoListingsException;
import tools.sctrade.companion.utils.LocalizationUtil;

public class ItemSubmissionFactory {
  private static final String COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS =
      "Could not find '{}' fragment. Falling back to default bounds";
  private static final String WALLET = "wallet:";
  private static final String CHOOSE_CATEGORY = "choose category";
  private static final String SELL = "sell";

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
    var location = extractLocation(screenCapture, ocrResult);

    var walletFragment = findWalletFragment(ocrResult);

    var shop = extractShop(ocrResult, walletFragment);

    Rectangle listingsBoundingBox = calculateListingsBoundingBox(ocrResult, walletFragment);
    var listingsOcrResult = ocrResult.crop(listingsBoundingBox);
    var listings = itemListingFactory.build(listingsOcrResult, location.orElse(null));

    if (listings.isEmpty()) {
      throw new NoListingsException();
    }

    return new ItemSubmission(userService.get(), listings);
  }

  private String extractShop(OcrResult ocrResult, Optional<LocatedFragment> walletFragment) {
    Rectangle shopBoundingBox = calculateShopBoundingBox(ocrResult, walletFragment);
    String shopText = ocrResult.crop(shopBoundingBox).getTextByLines().trim()
        .replaceAll(System.lineSeparator(), " ");
    logger.debug("Read shop text:\n{}", shopText);

    return shopText;
  }

  private Optional<String> extractLocation(BufferedImage screenCapture, OcrResult ocrResult) {
    Rectangle topLeftBoundingBox =
        new Rectangle(0, 0, (screenCapture.getWidth() / 2), (screenCapture.getHeight() / 3));
    var topLeftCornerOcrResult = ocrResult.crop(topLeftBoundingBox);
    var location = itemLocationReader.read(topLeftCornerOcrResult);

    if (location.isEmpty()) {
      notificationService.warn(LocalizationUtil.get("warnNoLocation"));
    }

    return location;
  }

  private Rectangle calculateShopBoundingBox(OcrResult ocrResult,
      Optional<LocatedFragment> walletFragment) {
    var sellFragment = findFragment(ocrResult, SELL);
    var chooseDestinationFragment = findFragment(ocrResult, CHOOSE_CATEGORY);

    int minX;
    int maxX;
    int minY = (int) ocrResult.getBoundingBox().getMinY();
    int maxY;

    if (sellFragment.isPresent()) {
      minX = (int) sellFragment.get().getBoundingBox().getMaxX();
    } else {
      logger.warn(COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS, SELL);
      minX = (int) (ocrResult.getBoundingBox().getMinX() * 0.33);
    }

    if (walletFragment.isPresent()) {
      maxX = (int) walletFragment.get().getBoundingBox().getMinX();
    } else {
      logger.warn(COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS, WALLET);
      maxX = (int) (ocrResult.getBoundingBox().getMaxX() * 0.66);
    }

    if (chooseDestinationFragment.isPresent()) {
      maxY = (int) chooseDestinationFragment.get().getBoundingBox().getMinY();
    } else {
      logger.warn(COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS,
          ItemLocationReader.CHOOSE_DESTINATION);
      maxY = (int) ocrResult.getBoundingBox().getMaxY() / 4;
    }

    int width = maxX - minX;
    int height = maxY - minY;

    return new Rectangle(minX, minY, width, height);
  }

  private Rectangle calculateListingsBoundingBox(OcrResult ocrResult,
      Optional<LocatedFragment> walletFragment) {
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
      logger.warn(COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS, CHOOSE_CATEGORY);
      minX = (int) ocrResult.getBoundingBox().getMinX();
      minY = (int) ocrResult.getBoundingBox().getMinY();
    }

    if (walletFragment.isPresent()) {
      maxX = (int) walletFragment.get().getBoundingBox().getMinX();
    } else {
      logger.warn(COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS, WALLET);
      maxX = (int) ocrResult.getBoundingBox().getMaxX();
    }

    int width = maxX - minX;
    int height = maxY - minY;

    return new Rectangle(minX, minY, width, height);
  }

  private Optional<LocatedFragment> findFragment(OcrResult ocrResult, String fragment) {
    try {
      return Optional.of(OcrUtil.findFragmentClosestTo(ocrResult, fragment));
    } catch (NoCloseStringException e) {
      logger.warn("Could not find '{}' fragment", fragment);
      return Optional.empty();
    }
  }

  private Optional<LocatedFragment> findWalletFragment(OcrResult ocrResult) {
    return ocrResult.getFragments().parallelStream()
        .filter(n -> n.getText().trim().startsWith(WALLET)).findFirst();
  }
}
