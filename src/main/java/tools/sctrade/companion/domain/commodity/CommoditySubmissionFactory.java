package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.StatusTrackingSubmissionFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.screenshot.ScreenshotRepository;
import tools.sctrade.companion.domain.screenshot.ScreenshotType;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.NoListingsException;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Factory for building commodity submissions.
 */
public class CommoditySubmissionFactory
    extends StatusTrackingSubmissionFactory<CommoditySubmission> {

  private final Logger logger = LoggerFactory.getLogger(CommoditySubmissionFactory.class);

  private UserService userService;
  private NotificationService notificationService;
  private CommodityListingFactory commodityListingFactory;
  private CommodityLocationReader commodityLocationReader;
  private Ocr ocr;

  /**
   * Constructor.
   *
   * @param screenshotRepository repository used to track screenshot processing status
   * @param screenshotType kiosk type to associate with tracked screenshots
   * @param userService the user service
   * @param notificationService the notification service
   * @param commodityLocationReader the commodity location reader
   * @param commodityListingFactory the commodity listing factory
   * @param ocr the OCR service
   */
  public CommoditySubmissionFactory(ScreenshotRepository screenshotRepository,
      ScreenshotType screenshotType, UserService userService,
      NotificationService notificationService, CommodityLocationReader commodityLocationReader,
      CommodityListingFactory commodityListingFactory, Ocr ocr) {
    super(screenshotRepository, screenshotType);
    this.userService = userService;
    this.notificationService = notificationService;
    this.commodityLocationReader = commodityLocationReader;
    this.commodityListingFactory = commodityListingFactory;
    this.ocr = ocr;
  }

  @Override
  protected CommoditySubmission doBuild(BufferedImage screenCapture) {
    var ocrResult = ocr.read(screenCapture);
    var location = commodityLocationReader.read(screenCapture, ocrResult);

    if (location.isEmpty()) {
      notificationService.warn(LocalizationUtil.get("warnNoLocation"));
    }

    Rectangle rightHalfBoundingBox = new Rectangle((screenCapture.getWidth() / 2), 0,
        (screenCapture.getWidth() - (screenCapture.getWidth() / 2)), screenCapture.getHeight());
    var rightHalfOcrResult = ocrResult.crop(rightHalfBoundingBox);
    var listings = commodityListingFactory.build(rightHalfOcrResult, location.orElse(null));

    if (listings.isEmpty()) {
      throw new NoListingsException();
    }

    return new CommoditySubmission(userService.get(), listings);
  }

  @Override
  protected String extractLocation(CommoditySubmission result) {
    return result.getListings().stream().map(CommodityListing::location).filter(l -> l != null)
        .findFirst().orElse(null);
  }

  @Override
  protected String extractContent(CommoditySubmission result) {
    return result.getListings().size() + " listings processed";
  }

  /**
   * Builds a commodity submission from a single listing (non-screenshot path, no status tracking).
   *
   * @param commodityListing the commodity listing to wrap
   * @return the commodity submission
   */
  public CommoditySubmission build(CommodityListing commodityListing) {
    return new CommoditySubmission(userService.get(), List.of(commodityListing));
  }
}
