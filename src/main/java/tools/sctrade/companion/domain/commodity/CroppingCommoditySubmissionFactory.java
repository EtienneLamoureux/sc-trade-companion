package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.NoListingsException;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Factory for building commodity submissions.
 */
public class CroppingCommoditySubmissionFactory implements CommoditySubmissionFactory {
  private final Logger logger = LoggerFactory.getLogger(CroppingCommoditySubmissionFactory.class);

  private UserService userService;
  private NotificationService notificationService;
  private CommodityListingFactory commodityListingFactory;
  private CommodityLocationReader commodityLocationReader;
  private Ocr ocr;

  /**
   * Constructor.
   *
   * @param userService the user service
   * @param notificationService the notification service
   * @param commodityLocationReader the commodity location reader
   * @param commodityListingFactory the commodity listing factory
   */
  public CroppingCommoditySubmissionFactory(UserService userService,
      NotificationService notificationService, CommodityLocationReader commodityLocationReader,
      CommodityListingFactory commodityListingFactory, Ocr ocr) {
    this.userService = userService;
    this.notificationService = notificationService;
    this.commodityLocationReader = commodityLocationReader;
    this.commodityListingFactory = commodityListingFactory;
    this.ocr = ocr;
  }

  @Override
  public CommoditySubmission build(BufferedImage screenCapture) {
    var ocrResult = ocr.read(screenCapture);
    Rectangle topLeftBoundingBox =
        new Rectangle(0, 0, (screenCapture.getWidth() / 2), (screenCapture.getHeight() / 3));
    var topLeftCornerOcrResult = ocrResult.crop(topLeftBoundingBox);
    var location = commodityLocationReader.read(topLeftCornerOcrResult);

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
  public CommoditySubmission build(CommodityListing commodityListing) {
    return new CommoditySubmission(userService.get(), List.of(commodityListing));
  }
}
