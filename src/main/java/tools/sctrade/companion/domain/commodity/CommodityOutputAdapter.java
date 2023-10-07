package tools.sctrade.companion.domain.commodity;

import java.util.Collection;

public interface CommodityOutputAdapter {
  public void publish(Collection<CommodityListing> listings);
}
