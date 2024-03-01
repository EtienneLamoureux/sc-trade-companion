package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.user.UserService;

public class CommoditySubmissionFactory {
  private final Logger logger = LoggerFactory.getLogger(CommoditySubmissionFactory.class);

  private UserService userService;
  private CommodityListingFactory commodityListingFactory;
  private CommodityLocationReader commodityLocationReader;

  public CommoditySubmissionFactory(UserService userService,
      NotificationService notificationService, CommodityRepository commodityRepository,
      LocationRepository locationRepository, ImageWriter imageWriter) {
    this.userService = userService;

    this.commodityLocationReader =
        new CommodityLocationReader(locationRepository, notificationService, imageWriter);
    this.commodityListingFactory = new CommodityListingFactory(commodityRepository, imageWriter);
  }

  CommoditySubmission build(BufferedImage screenCapture) {
    var location = commodityLocationReader.read(screenCapture);
    var listings = commodityListingFactory.build(screenCapture, location.get());

    return new CommoditySubmission(userService.get(), listings);
  }
}
