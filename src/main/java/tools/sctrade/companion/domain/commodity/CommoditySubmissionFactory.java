package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.image.manipulations.CommodityKioskTextThreshold;
import tools.sctrade.companion.domain.image.manipulations.ConvertToGreyscale;
import tools.sctrade.companion.domain.image.manipulations.InvertColors;
import tools.sctrade.companion.domain.image.manipulations.WriteToDisk;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.LocationNotFoundException;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.utils.LocalizationUtil;
import tools.sctrade.companion.utils.StringUtil;

public class CommoditySubmissionFactory {
  private static final String YOUR_INVENTORIES = "your inventories";

  private final Logger logger = LoggerFactory.getLogger(CommoditySubmissionFactory.class);

  private UserService userService;
  private NotificationService notificationService;
  private LocationRepository locationRepository;
  private ImageWriter imageWriter;
  private ThreadLocal<Ocr> locationOcr;
  private CommodityListingFactory commodityListingFactory;

  public CommoditySubmissionFactory(UserService userService,
      NotificationService notificationService, CommodityRepository commodityRepository,
      LocationRepository locationRepository, ImageWriter imageWriter) {
    this.userService = userService;
    this.notificationService = notificationService;
    this.locationRepository = locationRepository;
    this.imageWriter = imageWriter;
    this.commodityListingFactory = new CommodityListingFactory(commodityRepository, imageWriter);
    this.locationOcr = constructLocationOcr();
  }

  CommoditySubmission build(BufferedImage screenCapture) {
    try {
      logger.debug("Reading location...");
      OcrResult locationResult = locationOcr.get().read(screenCapture);
      var location = extractLocation(locationResult);
      logger.debug("Read location '{}'", location);

      var listings = commodityListingFactory.build(screenCapture, location);

      return new CommoditySubmission(userService.get(), listings);
    } finally {
      locationOcr.remove();
    }
  }

  private String extractLocation(OcrResult result) {
    List<LocatedFragment> fragments =
        result.getColumns().stream().flatMap(n -> n.getFragments().stream()).toList();
    var yourInventoriesFragment = OcrUtil.findFragmentClosestTo(fragments, YOUR_INVENTORIES);

    // Return the fragment that follows "your inventories"
    var it = fragments.iterator();

    while (it.hasNext()) {
      var next = it.next();

      if (next.equals(yourInventoriesFragment)) {
        String rawLocation = it.next().getText();
        logger.debug("Read location '{}'", rawLocation);

        try {
          return StringUtil.spellCheck(rawLocation, locationRepository.findAllLocations());
        } catch (NoCloseStringException e) {
          logger.warn("Could not spell-check location '{}'", rawLocation);
          notificationService.warn(LocalizationUtil.get("warnNoLocation"));
          return null;
        } catch (NoSuchElementException e) {
          throw new LocationNotFoundException(fragments);
        }
      }
    }

    throw new LocationNotFoundException(fragments);
  }

  private ThreadLocal<Ocr> constructLocationOcr() {
    List<ImageManipulation> preprocessingManipulations = new ArrayList<>();
    preprocessingManipulations.add(new InvertColors());
    preprocessingManipulations.add(new ConvertToGreyscale());
    preprocessingManipulations.add(new CommodityKioskTextThreshold());
    preprocessingManipulations.add(new WriteToDisk(imageWriter));

    return ThreadLocal
        .withInitial(() -> new CommodityLocationTesseractOcr(preprocessingManipulations));
  }
}
