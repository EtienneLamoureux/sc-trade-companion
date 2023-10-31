package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.ocr.LocatedColumn;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.exceptions.NotEnoughColumnsException;
import tools.sctrade.companion.utils.ImageUtil;
import tools.sctrade.companion.utils.StringUtil;

public class CommoditySubmissionFactory {
  private static final String SHOP_INVENTORY = "shop inventory";

  private final Logger logger = LoggerFactory.getLogger(CommoditySubmissionFactory.class);

  private UserService userService;
  private Ocr listingsOcr;

  public CommoditySubmissionFactory(UserService userService, Ocr listingsOcr) {
    this.userService = userService;
    this.listingsOcr = listingsOcr;
  }

  CommoditySubmission build(BufferedImage screenCapture) {
    OcrResult result = listingsOcr.read(screenCapture);
    var transactionType = extractTransactionType(screenCapture, result);
    var rawListings = buildRawListings(result);

    return null;
  }

  private List<RawCommodityListing> buildRawListings(OcrResult result) {
    var columns = result.getColumns();

    if (columns.size() < 2) {
      throw new NotEnoughColumnsException(2, result);
    }

    // Find the 2 largest columns, by line count
    var columnsBySizeDesc = new TreeMap<Integer, LocatedColumn>(Collections.reverseOrder());
    columns.forEach(n -> columnsBySizeDesc.put(n.getText().length(), n));
    var columnIterator = columnsBySizeDesc.values().iterator();
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
    Map<Integer, LocatedFragment> fragmentsByDistanceToTarget = new ConcurrentSkipListMap<>();
    result.getColumns().parallelStream().flatMap(n -> n.getFragments().parallelStream())
        .forEach(n -> fragmentsByDistanceToTarget
            .put(StringUtil.calculateLevenshteinDistance(n.getText(), SHOP_INVENTORY), n));

    if (fragmentsByDistanceToTarget.keySet().iterator().next() > (SHOP_INVENTORY.length() / 2)) {
      throw new NoCloseStringException(SHOP_INVENTORY);
    }

    return fragmentsByDistanceToTarget.values().iterator().next().getBoundingBox();
  }
}
