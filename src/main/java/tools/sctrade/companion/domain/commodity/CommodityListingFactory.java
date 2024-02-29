package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.image.manipulations.CommodityKioskTextThreshold;
import tools.sctrade.companion.domain.image.manipulations.ConvertToGreyscale;
import tools.sctrade.companion.domain.image.manipulations.InvertColors;
import tools.sctrade.companion.domain.image.manipulations.WriteToDisk;
import tools.sctrade.companion.domain.ocr.LocatedColumn;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.exceptions.NoListingsException;
import tools.sctrade.companion.exceptions.NotEnoughColumnsException;
import tools.sctrade.companion.utils.HashUtil;
import tools.sctrade.companion.utils.StringUtil;
import tools.sctrade.companion.utils.TimeUtil;

public class CommodityListingFactory {
  private final Logger logger = LoggerFactory.getLogger(CommodityListingFactory.class);

  private TransactionTypeExtractor transactionTypeExtractor;
  private CommodityRepository commodityRepository;
  private ImageWriter imageWriter;
  private ThreadLocal<Ocr> listingsOcr;

  public CommodityListingFactory(CommodityRepository commodityRepository, ImageWriter imageWriter) {
    this.transactionTypeExtractor = new TransactionTypeExtractor(imageWriter);
    this.commodityRepository = commodityRepository;
    this.imageWriter = imageWriter;
    this.listingsOcr = constructListingsOcr();
  }

  public Collection<CommodityListing> build(BufferedImage screenCapture, String location) {
    try {
      logger.debug("Reading listings...");
      OcrResult listingsResult = listingsOcr.get().read(screenCapture);
      var rawListings = buildRawListings(listingsResult);
      logger.debug("Read {} listings", rawListings.size());

      TransactionType transactionType =
          transactionTypeExtractor.extract(screenCapture, listingsResult);
      String batchId = HashUtil.hash(screenCapture);

      return buildCommodityListings(location, transactionType, rawListings, batchId);
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

    if (rawListings.isEmpty()) {
      throw new NoListingsException();
    }

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

  private ThreadLocal<Ocr> constructListingsOcr() {
    List<ImageManipulation> preprocessingManipulations = new ArrayList<>();
    preprocessingManipulations.add(new InvertColors());
    preprocessingManipulations.add(new ConvertToGreyscale());
    preprocessingManipulations.add(new CommodityKioskTextThreshold());
    preprocessingManipulations.add(new WriteToDisk(imageWriter));

    return ThreadLocal
        .withInitial(() -> new CommodityListingsTesseractOcr(preprocessingManipulations));
  }
}
