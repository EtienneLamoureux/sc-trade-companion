package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.ocr.LocatedColumn;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.LocationNotFoundException;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.exceptions.NoListingsException;
import tools.sctrade.companion.exceptions.NotEnoughColumnsException;
import tools.sctrade.companion.utils.HashUtil;
import tools.sctrade.companion.utils.ImageUtil;
import tools.sctrade.companion.utils.StringUtil;

public class CommoditySubmissionFactory {
  private static final String SHOP_INVENTORY = "shop inventory";
  private static final String YOUR_INVENTORIES = "your inventories";

  private final Logger logger = LoggerFactory.getLogger(CommoditySubmissionFactory.class);

  private UserService userService;
  private Ocr listingsOcr;
  private Ocr locationOcr;

  public CommoditySubmissionFactory(UserService userService, Ocr listingsOcr, Ocr locationOcr) {
    this.userService = userService;
    this.listingsOcr = listingsOcr;
    this.locationOcr = locationOcr;
  }

  CommoditySubmission build(BufferedImage screenCapture) {
    OcrResult listingsResult = listingsOcr.read(screenCapture);
    var rawListings = buildRawListings(listingsResult);
    var transactionType = extractTransactionType(screenCapture, listingsResult);

    OcrResult locationResult = locationOcr.read(screenCapture);
    var location = extractLocation(locationResult);
    String batchId = HashUtil.hash(screenCapture);

    Collection<CommodityListing> listings =
        buildCommodityListings(location, transactionType, rawListings, batchId);

    return new CommoditySubmission(userService.get(), listings);
  }


  private Collection<CommodityListing> buildCommodityListings(String location,
      TransactionType transactionType, List<RawCommodityListing> rawListings, String batchId) {
    rawListings = rawListings.parallelStream().filter(n -> n.isComplete()).toList();

    if (rawListings.isEmpty()) {
      throw new NoListingsException();
    }

    Instant now = Instant.now();

    return rawListings.parallelStream()
        .map(n -> new CommodityListing(location, transactionType.toString(), n.getCommodity().get(),
            n.getPrice().get(), n.getInventory().get(), n.getInventoryLevel().get().getSaturation(),
            batchId, now))
        .toList();
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

    return rawListings;
  }

  private TransactionType extractTransactionType(BufferedImage screenCapture, OcrResult result) {
    screenCapture = ImageUtil.makeGreyscaleCopy(screenCapture);
    Rectangle shopInv = getShopInventoryRectangle(result);

    int y = (int) shopInv.getMaxY();
    int width = (int) shopInv.getWidth();
    int height = (int) shopInv.getHeight() * 3;

    var buyRectangle = new Rectangle((int) shopInv.getMinX(), y, width, height);
    var buyRectangleColor = ImageUtil.calculateAverageColor(screenCapture, buyRectangle);
    var buyRectangleLuminance = buyRectangleColor.getRed();

    var sellRectangle =
        new Rectangle((int) (shopInv.getMaxX() + (shopInv.getWidth() / 4)), y, width, height);
    var sellRectangleColor = ImageUtil.calculateAverageColor(screenCapture, sellRectangle);
    var sellRectangleLuminance = sellRectangleColor.getRed();

    return (buyRectangleLuminance > sellRectangleLuminance) ? TransactionType.SELLS
        : TransactionType.BUYS;
  }

  private Rectangle getShopInventoryRectangle(OcrResult result) {
    var fragments = result.getColumns().parallelStream()
        .flatMap(n -> n.getFragments().parallelStream()).toList();
    LocatedFragment shopInventoryFragment = findFragmentClosestTo(fragments, SHOP_INVENTORY);

    return shopInventoryFragment.getBoundingBox();
  }

  private String extractLocation(OcrResult result) {
    List<LocatedColumn> columns = result.getColumns();
    var columnIterator = getColumnIteratorOrderedByLineCount(columns);
    var longestColumn = columnIterator.next();

    var yourInventoriesFragment =
        findFragmentClosestTo(longestColumn.getFragments(), YOUR_INVENTORIES);

    // Return the fragment that follows "your inventories"
    var it = longestColumn.getFragments().iterator();

    while (it.hasNext()) {
      var next = it.next();

      if (next.equals(yourInventoriesFragment)) {
        try {
          return it.next().getText();
        } catch (NoSuchElementException e) {
          throw new LocationNotFoundException(longestColumn);
        }
      }
    }

    throw new LocationNotFoundException(longestColumn);
  }

  private LocatedFragment findFragmentClosestTo(Collection<LocatedFragment> fragments,
      String string) {
    Map<Integer, LocatedFragment> fragmentsByDistanceToTarget = new ConcurrentSkipListMap<>();
    fragments.parallelStream().forEach(n -> fragmentsByDistanceToTarget
        .put(StringUtil.calculateLevenshteinDistance(n.getText(), string), n));

    if (fragmentsByDistanceToTarget.keySet().iterator().next() > (string.length() / 2)) {
      throw new NoCloseStringException(string);
    }

    return fragmentsByDistanceToTarget.values().iterator().next();
  }

  private Iterator<LocatedColumn> getColumnIteratorOrderedByLineCount(List<LocatedColumn> columns) {
    var columnsBySizeDesc = new TreeMap<Integer, LocatedColumn>(Collections.reverseOrder());
    columns.forEach(n -> columnsBySizeDesc.put(n.getText().length(), n));

    return columnsBySizeDesc.values().iterator();
  }
}
