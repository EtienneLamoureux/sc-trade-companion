package tools.sctrade.companion.domain.commodity;

import java.util.Collection;
import org.springframework.scheduling.annotation.Async;

public interface CommodityOutputAdapter {
  @Async
  public void publishAsynchronously(Collection<CommodityListing> listings);
}
