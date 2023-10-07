package tools.sctrade.companion.domain.commodity;

import java.util.Collection;

public class CommodityService {
  private Collection<CommodityOutputAdapter> outputAdapters;

  public CommodityService(Collection<CommodityOutputAdapter> outputAdapters) {
    this.outputAdapters = outputAdapters;
  }

  private void publish(Collection<CommodityListing> listings) {
    outputAdapters.parallelStream().forEach(n -> n.publish(listings));
  }
}
