package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.ocr.LocatedColumn;
import tools.sctrade.companion.domain.ocr.Ocr;
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

  private TransactionTypeExtractor transactionTypeExtractor;
  private CommodityRepository commodityRepository;
  private ThreadLocal<Ocr> listingsOcr;

  /**
   * Constructor for {@link CommodityListingFactory}.
   *
   * @param commodityRepository Repository to get the commodity names from. Will be used to spell
   *        check the OCR results.
   * @param imageWriter Output port for images.
   * @param preprocessingManipulations List of image manipulations to be applied, sequentially and
   *        in order, before running the commodity OCR.
   */
  public CommodityListingFactory(CommodityRepository commodityRepository, ImageWriter imageWriter,
      List<ImageManipulation> preprocessingManipulations) {
    this.transactionTypeExtractor = new TransactionTypeExtractor(imageWriter);
    this.commodityRepository = commodityRepository;
    this.listingsOcr = ThreadLocal
        .withInitial(() -> new CommodityListingsTesseractOcr(preprocessingManipulations));
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
    return new CommodityListing(location, TransactionType.SELLS, commodity, maxBoxSizeInScu,
        batchId, now);
  }

  /**
   * Runs OCR on the image and assembles the readable commodity listings.
   *
   * @param screenCapture Image of the section of the commodity kiosk with the commodity listings
   * @param location Pre-parsed location string, as it would appear on the left-side of the
   *        commodity kiosk. See {@link CommodityLocationReader}.
   * @return Final, assembled commodity listings
   */
  public Collection<CommodityListing> build(BufferedImage screenCapture, String location) {
    try {
      logger.debug("Reading listings...");
      OcrResult listingsResult = listingsOcr.get().read(screenCapture);
      listingsResult = removeNonListingWords(listingsResult);
      var rawListings = buildRawListings(listingsResult);
      logger.debug("Read {} listings", rawListings.size());

      TransactionType transactionType =
          transactionTypeExtractor.extract(screenCapture, listingsResult);
      String batchId = HashUtil.hash(screenCapture);

      return buildCommodityListings(location, transactionType, rawListings, batchId);
    } catch (Exception e) {
      logger.error("Error while reading listings", e);
      // TODO
      return Collections.emptyList();
    } finally {
      listingsOcr.remove();
    }
  }

  private List<RawCommodityListing> buildRawListings(OcrResult result) {
    var columns = result.getColumns();

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

  private OcrResult removeNonListingWords(OcrResult result) {
    var shopInventoryFragment = OcrUtil.findFragmentClosestTo(result, SHOP_INVENTORY);
    Rectangle shopInventoryRectangle = shopInventoryFragment.getBoundingBox();
    double minX =
        shopInventoryRectangle.getMinX() + (2 * shopInventoryFragment.getCharacterWidth());

    var words = result.getColumns().stream().flatMap(n -> n.getFragments().stream())
        .flatMap(n -> n.getWordsInReadingOrder().stream())
        .filter(n -> n.getBoundingBox().getMinX() > minX)
        .filter(n -> n.getBoundingBox().getMinY() > shopInventoryRectangle.getMinY()).toList();

    return new OcrResult(words);
  }

  private List<RawCommodityListing> assembleRawListings(List<LocatedColumn> leftHalfListings,
      List<LocatedColumn> rightHalfListings) {
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

  private Collection<CommodityListing> buildCommodityListings(String location,
      TransactionType transactionType, List<RawCommodityListing> rawListings, String batchId) {
    rawListings = rawListings.parallelStream().filter(n -> n.isComplete()).toList();

    Instant now = TimeUtil.getNow();

    return rawListings.parallelStream().map(n -> {
      try {
        return Optional.of(new CommodityListing(location, transactionType,
            StringUtil.spellCheck(n.getCommodity().get(), commodityRepository.findAllCommodities()),
            n.getPrice().get(), n.getInventory().get(), n.getInventoryLevel().get(), batchId, now));
      } catch (NoCloseStringException e) {
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
