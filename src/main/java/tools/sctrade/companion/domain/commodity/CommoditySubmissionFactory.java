package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.NoListingsException;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Factory for building commodity submissions.
 */
public class CommoditySubmissionFactory {
  private final Logger logger = LoggerFactory.getLogger(CommoditySubmissionFactory.class);

  private UserService userService;
  private NotificationService notificationService;
  private CommodityListingFactory commodityListingFactory;
  private CommodityLocationReader commodityLocationReader;

  /**
   * Constructor.
   *
   * @param userService the user service
   * @param notificationService the notification service
   * @param commodityLocationReader the commodity location reader
   * @param commodityListingFactory the commodity listing factory
   */
  public CommoditySubmissionFactory(UserService userService,
      NotificationService notificationService, CommodityLocationReader commodityLocationReader,
      CommodityListingFactory commodityListingFactory) {
    this.userService = userService;
    this.notificationService = notificationService;
    this.commodityLocationReader = commodityLocationReader;
    this.commodityListingFactory = commodityListingFactory;
  }

  CommoditySubmission build(BufferedImage screenCapture) {
    var location = commodityLocationReader.read(screenCapture);

    if (location.isEmpty()) {
      notificationService.warn(LocalizationUtil.get("warnNoLocation"));
    }

    var listings = commodityListingFactory.build(screenCapture, location.orElse(null));

    if (listings.isEmpty()) {
      throw new NoListingsException();
    }

    return new CommoditySubmission(userService.get(), listings);
  }

  CommoditySubmission build(CommodityListing commodityListing) {
    return new CommoditySubmission(userService.get(), List.of(commodityListing));
  }
}
