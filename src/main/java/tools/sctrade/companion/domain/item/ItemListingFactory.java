package tools.sctrade.companion.domain.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.utils.StringUtil;

public class ItemListingFactory {
  private static final Pattern LISTING_PATTERN =
      Pattern.compile("((?:.+\\R){0,2}.+)\\Rvolume:[^\\r\\n]*\\R[\\W ]*([0-9,]+)");

  private final Logger logger = LoggerFactory.getLogger(ItemListingFactory.class);

  private final ItemRepository itemRepository;

  public ItemListingFactory(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  public Collection<ItemListing> build(OcrResult ocrResult, String location) {
    var listings = new ArrayList<ItemListing>();

    for (var column : ocrResult.getColumns()) {
      String text = column.getText();
      listings.addAll(buildFromText(text, location));
    }

    return spellCheckListings(listings);
  }

  private List<ItemListing> buildFromText(String text, String location) {
    logger.trace("Reading item listings from text:\n{}", text);
    var listings = new ArrayList<ItemListing>();
    var matcher = LISTING_PATTERN.matcher(text);

    while (matcher.find()) {
      String name = matcher.group(1).replaceAll(System.lineSeparator(), " ").trim();
      double price = Double.parseDouble(matcher.group(2).replace(",", ""));
      var listing = new ItemListing(name, price, location);
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
