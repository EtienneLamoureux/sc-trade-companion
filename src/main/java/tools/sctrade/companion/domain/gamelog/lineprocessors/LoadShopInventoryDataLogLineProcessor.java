package tools.sctrade.companion.domain.gamelog.lineprocessors;

import java.util.Locale;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.commodity.CommodityListingFactory;
import tools.sctrade.companion.domain.commodity.CommodityService;
import tools.sctrade.companion.domain.gamelog.GameLogLineProcessor;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * This class is responsible for processing log lines that contain information about the inventory
 * of a shop. Those log records are processed into commodity listings.
 */
public class LoadShopInventoryDataLogLineProcessor extends GameLogLineProcessor {
  private final Logger logger =
      LoggerFactory.getLogger(LoadShopInventoryDataLogLineProcessor.class);

  private NotificationService notificationService;
  private CommodityListingFactory commodityListingFactory;
  private CommodityService commodityService;

  /**
   * Constructor.
   *
   * @param commodityListingFactory Factory for creating commodity listings
   * @param commodityService Service for processing commodity listings
   * @param notificationService Service for sending notifications
   */
  public LoadShopInventoryDataLogLineProcessor(CommodityListingFactory commodityListingFactory,
      CommodityService commodityService, NotificationService notificationService) {
    this.regex =
        ".+LoadShopInventoryData.+shopId\\[(?<shopId>\\d+)\\] shopName\\[(?<shopName>[\\w-]+)\\] commodityName\\[ResourceType\\.(?<commodityName>[\\w-]+)\\].+boxSize\\[(?<maxBoxSize>\\d+)\\] \\[Team_NAPU\\]\\[Shops\\]\\[UI\\]";
    this.commodityListingFactory = commodityListingFactory;
    this.commodityService = commodityService;
    this.notificationService = notificationService;
  }

  @Override
  protected void handle(String value) {
    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(value);
    matcher.matches();

    var shopId = matcher.group("shopId");
    var shopName = matcher.group("shopName");
    var commodityName = matcher.group("commodityName");
    var maxBoxSize = Integer.valueOf(matcher.group("maxBoxSize"));

    logger.info("Shop {}#{} sells {} in boxes of up to {} SCU", shopName, shopId, commodityName,
        maxBoxSize);
    notificationService
        .info(String.format(Locale.ROOT, LocalizationUtil.get("infoLocationDetectedFromLogs"),
            String.format(Locale.ROOT, "%s#%s", shopName, shopId)));

    var commodityListing =
        commodityListingFactory.build(shopId, shopName, commodityName, maxBoxSize);

    try {
      commodityService.process(commodityListing);
    } catch (InterruptedException e) {
      logger.error("Could not send {} for processing", commodityListing, e);
    }
  }
}
