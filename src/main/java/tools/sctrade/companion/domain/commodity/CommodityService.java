package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageProcessor;
import tools.sctrade.companion.domain.ocr.LocatedColumn;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.user.UserService;

public class CommodityService extends ImageProcessor {
  private final Logger logger = LoggerFactory.getLogger(CommodityService.class);

  private UserService userService;
  private Ocr ocr;
  private Collection<CommodityPublisher> outputAdapters;
  private final String SEARCH = "([0-9])[\\.\\, ]{2,}([0-9])";
  private final String SEARCH2 = "$1\\.$2";
  private final String SEARCH3 =
      "([a-z ]+) ([0-9,]+ |os).+\\R(.+) [^0-9]?(([0-9]+[\\.\\,])?[0-9]+([k ]+)?)\\/";

  public CommodityService(UserService userService, Ocr ocr,
      Collection<CommodityPublisher> outputAdapters) {
    this.userService = userService;
    this.ocr = ocr;
    this.outputAdapters = outputAdapters;
  }

  @Override
  public void process(BufferedImage screenCapture) {
    OcrResult result = ocr.read(screenCapture);

    var columns = result.getColumns();

    if (columns.size() < 2) {
      logger.error("Could not make out 2 or more columns of text in the commodity listings");
      return;
    }

    var columnsBySizeDesc = new TreeMap<Integer, LocatedColumn>(Collections.reverseOrder());
    columns.forEach(n -> columnsBySizeDesc.put(n.getText().length(), n));
    var columnIterator = columnsBySizeDesc.values().iterator();
    var column1 = columnIterator.next();
    var column2 = columnIterator.next();

    List<LocatedColumn> leftHalfListings;
    Vector<LocatedColumn> rightHalfListings;

    if (column1.getBoundingBox().getCenterX() < column2.getBoundingBox().getCenterX()) {
      leftHalfListings = column1.getParagraphs();
      rightHalfListings = new Vector<>(column2.getParagraphs());
    } else {
      leftHalfListings = column2.getParagraphs();
      rightHalfListings = new Vector<>(column1.getParagraphs());
    }

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

    publish(Collections.emptyList()); // TODO
  }

  private void publish(Collection<CommodityListing> listings) {
    var user = userService.get();
    var submission = new CommoditySubmission(user, listings);

    outputAdapters.stream().forEach(n -> n.publishAsynchronously(submission));
  }
}
