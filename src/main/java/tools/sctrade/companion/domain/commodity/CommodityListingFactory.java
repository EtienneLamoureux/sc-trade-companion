package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.ocr.LocatedColumn;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.exceptions.NotEnoughColumnsException;
import tools.sctrade.companion.utils.HashUtil;
import tools.sctrade.companion.utils.StringUtil;
import tools.sctrade.companion.utils.TimeUtil;

/**
 * Uses OCR to read and assemble a collection of {@link CommodityListing} from the image of a
 * commodity kiosk.
 */
public class CommodityListingFactory {
  private final Logger logger = LoggerFactory.getLogger(CommodityListingFactory.class);
  private static final String SHOP_INVENTORY = "shop inventory";
  private static final String AVAILABLE_CARGO_SIZE_SCU = "available cargo size (scu)";

  private TransactionTypeExtractor transactionTypeExtractor;
  private CommodityRepository commodityRepository;

  /**
   * Constructor for {@link CommodityListingFactory}.
   *
   * @param commodityRepository Repository to get the commodity names from. Will be used to spell
   *        check the OCR results.
   */
  public CommodityListingFactory(CommodityRepository commodityRepository) {
    this.transactionTypeExtractor = new TransactionTypeExtractor();
    this.commodityRepository = commodityRepository;
  }

  /**
   * Assembles a {@link CommodityListing} from the information taken from a game's log record.
   *
   * @param shopId Internal id as read from the game's logs
   * @param shopName Internal name as read from the game's logs
   * @param commodity Commodity name as read from the game's logs
   * @param maxBoxSizeInScu Largest handled box size for the listing described
   * @return An assembled commodity listing
   */
  public CommodityListing build(String shopId, String shopName, String commodity,
      int maxBoxSizeInScu) {
    Instant now = TimeUtil.getNow();
    String location = String.format(Locale.ROOT, "%s#%s", shopName, shopId);
    String batchId = HashUtil.hash(String.format(Locale.ROOT, "%s%s%d%s", location, commodity,
        maxBoxSizeInScu, now.toString()));
    return new CommodityListing(location, TransactionType.SELLS, commodity,
        List.of(maxBoxSizeInScu), batchId, now);
  }

  /**
   * Runs OCR on the image and assembles the readable commodity listings.
   *
   * @param listingsResult OcrResult cropped ocr results corresponding to the listings area of the
   *        commodity kiosk
   * @param location Pre-parsed location string, as it would appear on the left-side of the
   *        commodity kiosk. See {@link CommodityLocationReader}.
   * @return Final, assembled commodity listings
   */
  public Collection<CommodityListing> build(OcrResult listingsResult, String location) {
    try {
      logger.debug("Reading listings...");
      listingsResult = removeNonListingWords(listingsResult);
      listingsResult = removePureGibberishWords(listingsResult);
      var rawListings = buildRawListings(listingsResult);
      logger.debug("Read {} listings", rawListings.size());

      TransactionType transactionType = transactionTypeExtractor.extract(listingsResult);
      String batchId = HashUtil.hash(listingsResult);

      return buildCommodityListings(location, transactionType, rawListings, batchId);
    } catch (Exception e) {
      logger.error("Error while reading listings", e);
      // TODO
      return Collections.emptyList();
    }
  }

  private List<RawCommodityListing> buildRawListings(OcrResult result) {
    var columns = result.getTwoColumns();

    if (columns.size() < 2) {
      throw new NotEnoughColumnsException(2, result);
    }

    // Find the 2 largest columns, by line count
    var columnIterator = getColumnIteratorOrderedByLineCount(columns);
    var column1 = columnIterator.next();
    var column2 = columnIterator.next();

    // Assign left and right columns
    List<LocatedColumn> leftHalfListings;
    List<LocatedColumn> rightHalfListings;

    if (column1.getBoundingBox().getCenterX() < column2.getBoundingBox().getCenterX()) {
      leftHalfListings = column1.getParagraphs();
      rightHalfListings = new ArrayList<>(column2.getParagraphs());
    } else {
      leftHalfListings = column2.getParagraphs();
      rightHalfListings = new ArrayList<>(column1.getParagraphs());
    }

    return assembleRawListings(leftHalfListings, rightHalfListings);
  }

  /**
   * Removes text falling into one of those categories.
   * <ul>
   * <li>above the listings, i.e.
   * <ul>
   * <li>the player's balance</li>
   * <li>outside the commodity terminal</li>
   * <li>the debug info</li>
   * </ul>
   * </li>
   * <li>in-between the 2 sides of the commodity terminal, in the graphic part of the screen</li>
   * </ul>
   *
   * @param result OcrResult
   * @return OcrResult
   */
  private OcrResult removeNonListingWords(OcrResult result) {
    var shopInventoryFragment = OcrUtil.findFragmentClosestTo(result, SHOP_INVENTORY);
    Rectangle shopInventoryRectangle = shopInventoryFragment.getBoundingBox();
    double minX =
        shopInventoryRectangle.getMinX() + (2 * shopInventoryFragment.getCharacterWidth());
    double minY = shopInventoryRectangle.getMinY() + (3 * shopInventoryRectangle.getHeight());

    var words = result.getLines().stream().flatMap(n -> n.getFragments().stream())
        .flatMap(n -> n.getWordsInReadingOrder().stream())
        .filter(n -> n.getBoundingBox().getMinX() > minX)
        .filter(n -> n.getBoundingBox().getMinY() > minY).toList();

    return new OcrResult(words);
  }

  private OcrResult removePureGibberishWords(OcrResult result) {
    var words = result.getLines().stream().flatMap(n -> n.getFragments().stream())
        .flatMap(n -> n.getWordsInReadingOrder().stream())
        .filter(n -> n.getText().matches(".*[a-zA-Z0-9].*")).toList();

    return new OcrResult(words);
  }

  private List<RawCommodityListing> assembleRawListings(List<LocatedColumn> leftHalfListings,
      List<LocatedColumn> rightHalfListings) {
    rightHalfListings = mergeAvailableBoxSizeParagraphs(rightHalfListings);
    List<RawCommodityListing> rawListings = new ArrayList<>();

    for (var leftHalfListing : leftHalfListings) {
      for (var rightHalfListing : rightHalfListings) {
        if (leftHalfListing.hasYOverlapWith(rightHalfListing)) {
          rawListings.add(new RawCommodityListing(leftHalfListing, rightHalfListing));
          rightHalfListings.remove(rightHalfListing);
          break;
        }
      }
    }

    logger.info("Read {} commodity listings", rawListings.size());

    return rawListings;
  }

  /**
   * Since the "Available cargo size" section of the listing can be considered different paragraphs
   * by the paragraph-splitter during post-processing, it needs to be merged to the quantity and
   * price section. <br />
   * Example:
   * 
   * <pre>
   * shop quantity
   * 263 scu
   * 2.01900005k/scu
   * </pre>
   * 
   * <pre>
   * available cargd size iscui
   * 1 2 4 8 16
   * </pre>
   * 
   * Becomes:
   * 
   * <pre>
   * shop quantity
   * 263 scu
   * 2.01900005k/scu
   * available cargd size iscui
   * 1 2 4 8 16
   * </pre>
   * 
   * Or
   * 
   * <pre>
   * shop quantity
   * 263 scu
   * 2.01900005k/scu
   * </pre>
   * 
   * <pre>
   * available cargd size iscui
   * </pre>
   * 
   * <pre>
   * 1 2 4 8 16
   * </pre>
   * 
   * Becomes:
   * 
   * <pre>
   * shop quantity
   * 263 scu
   * 2.01900005k/scu
   * available cargd size iscui
   * 1 2 4 8 16
   * </pre>
   *
   * @param rightHalfListings Whole right column, including all quantity, prices and available cargo
   *        sizes.
   * @return Merged paragraphs
   */
  private List<LocatedColumn> mergeAvailableBoxSizeParagraphs(
      List<LocatedColumn> rightHalfListings) {
    var mergedRightHalfListings = new ArrayList<LocatedColumn>();

    for (var rightHalfListing : rightHalfListings) {
      var fragments = rightHalfListing.getFragments();

      if (!mergedRightHalfListings.isEmpty() && fragments.size() == 2
          && isAvailableBoxSizeParagraph(fragments)) {
        // Merges both text and box size lines
        var previousParagraph = mergedRightHalfListings.get(mergedRightHalfListings.size() - 1);
        fragments.stream().forEach(n -> previousParagraph.add(n));
      } else if (!mergedRightHalfListings.isEmpty() && fragments.size() == 1
          && isAvailableBoxSizeParagraph(fragments)) {
        // Merges text line only
        var previousParagraph = mergedRightHalfListings.get(mergedRightHalfListings.size() - 1);
        fragments.stream().forEach(n -> previousParagraph.add(n));
      } else if (!mergedRightHalfListings.isEmpty() && fragments.size() == 1
          && fragments.getLast().isNumerical()
          && isAvailableBoxSizeParagraph(mergedRightHalfListings.getLast().getFragments())) {
        // Merges box size line only
        var previousParagraph = mergedRightHalfListings.getLast();
        fragments.stream().forEach(n -> previousParagraph.add(n));
      } else {
        mergedRightHalfListings.add(rightHalfListing);
      }
    }

    return mergedRightHalfListings;
  }

  private boolean isAvailableBoxSizeParagraph(List<LocatedFragment> fragments) {
    try {
      OcrUtil.findFragmentClosestTo(fragments, AVAILABLE_CARGO_SIZE_SCU);

      return true;
    } catch (NoCloseStringException e) {
      return false;
    }
  }

  private Collection<CommodityListing> buildCommodityListings(String location,
      TransactionType transactionType, List<RawCommodityListing> rawListings, String batchId) {
    rawListings = rawListings.parallelStream().filter(n -> n.isComplete()).toList();

    Instant now = TimeUtil.getNow();

    return rawListings.parallelStream().map(n -> {
      try {
        return Optional.of(new CommodityListing(location, transactionType,
            StringUtil.spellCheck(n.getCommodity().get(), commodityRepository.findAllCommodities()),
            n.getPrice().get(), n.getInventory().get(), n.getInventoryLevel().get(),
            n.getBoxSizesInScu().orElse(List.of()), batchId, now));
      } catch (NoCloseStringException | NoSuchElementException e) {
        return Optional.empty();
      }
    }).filter(n -> n.isPresent()).map(n -> (CommodityListing) n.get()).toList();
  }

  private Iterator<LocatedColumn> getColumnIteratorOrderedByLineCount(List<LocatedColumn> columns) {
    var columnsBySizeDesc = new TreeMap<Integer, LocatedColumn>(Collections.reverseOrder());
    columns.forEach(n -> columnsBySizeDesc.put(n.getText().length(), n));

    return columnsBySizeDesc.values().iterator();
  }
}
