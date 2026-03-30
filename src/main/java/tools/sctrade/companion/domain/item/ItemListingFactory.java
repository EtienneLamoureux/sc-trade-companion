package tools.sctrade.companion.domain.item;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.utils.StringUtil;

public class ItemListingFactory {
  private static final Pattern LISTING_PATTERN =
      Pattern.compile("((?:.+\\R){0,2}.+)\\Rvolume:[^\\r\\n]*\\R[\\Wa-zA-Z0 ]*([0-9,]+)");
  private static final String WALLET = "wallet:";
  private static final String CHOOSE_CATEGORY = "choose category";
  private static final String COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS =
      "Could not find '{}' fragment. Falling back to default bounds";

  private final Logger logger = LoggerFactory.getLogger(ItemListingFactory.class);

  private final ItemRepository itemRepository;

  public ItemListingFactory(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  public Collection<ItemListing> build(OcrResult ocrResult, String location, String shop) {
    var listingsOcrResult = ocrResult.crop(calculateListingsBoundingBox(ocrResult));
    var listings = new ArrayList<ItemListing>();

    for (var column : listingsOcrResult.getColumns()) {
      String text = column.getText();
      listings.addAll(buildFromText(text, location, shop));
    }

    return spellCheckListings(listings);
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
      logger.warn(COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS, CHOOSE_CATEGORY);
      minX = (int) ocrResult.getBoundingBox().getMinX();
      minY = (int) ocrResult.getBoundingBox().getMinY();
    }

    Optional<LocatedFragment> walletFragment = findWalletFragment(ocrResult);

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

  private Optional<LocatedFragment> findWalletFragment(OcrResult ocrResult) {
    return ocrResult.getFragments().parallelStream()
        .filter(n -> n.getText().trim().startsWith(WALLET)).findFirst();
  }

  private List<ItemListing> buildFromText(String text, String location, String shop) {
    logger.trace("Reading item listings from text:\n{}", text);
    var listings = new ArrayList<ItemListing>();
    var matcher = LISTING_PATTERN.matcher(text);

    while (matcher.find()) {
      String name =
          matcher.group(1).replace("quick buy", "").replaceAll(System.lineSeparator(), " ").trim();
      double price = Double.parseDouble(matcher.group(2).replace(",", ""));
      var listing = new ItemListing(name, price, location, shop);
      listings.add(listing);
      logger.debug("Read item listing: {}", listing);
    }

    return listings;
  }

  private List<ItemListing> spellCheckListings(List<ItemListing> rawListings) {
    List<String> knownItems = itemRepository.findAllItems();
    var listings = new ArrayList<ItemListing>();

    for (var listing : rawListings) {
      if (knownItems.contains(listing.name())) {
        listings.add(listing);
        continue;
      }

      try {
        String spellChecked = StringUtil.spellCheck(listing.name(), knownItems);
        listings.add(listing.withName(spellChecked));
      } catch (NoCloseStringException e) {
        logger.warn("Could not spell-check item name '{}', skipping", listing.name());
      }
    }

    return listings;
  }

}
