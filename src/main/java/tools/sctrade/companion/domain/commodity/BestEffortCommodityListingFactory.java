package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class BestEffortCommodityListingFactory extends CommodityListingFactory {
  private Collection<CommodityListingFactory> commodityListingFactory;

  public BestEffortCommodityListingFactory(
      Collection<CommodityListingFactory> commodityListingFactory) {
    super(null, null, Collections.emptyList());
    this.commodityListingFactory = commodityListingFactory;
  }

  @Override
  public Collection<CommodityListing> build(BufferedImage screenCapture, String location) {
    var commodityListings = commodityListingFactory.stream().map((n) -> {
      try {
        return n.build(screenCapture, location);
      } catch (Exception e) {
        return new ArrayList<CommodityListing>();
      }
    }).collect(Collectors.toList());

    commodityListings.sort((n, m) -> Integer.compare(m.size(), n.size()));

    return commodityListings.get(0);
  }
}
