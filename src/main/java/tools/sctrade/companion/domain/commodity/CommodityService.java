package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import org.springframework.scheduling.annotation.Async;

public class CommodityService {
  private Collection<CommodityOutputAdapter> outputAdapters;

  public CommodityService(Collection<CommodityOutputAdapter> outputAdapters) {
    this.outputAdapters = outputAdapters;
  }

  @Async
  public void processAsynchronously(BufferedImage screenCapture) {

  }

  private void publish(Collection<CommodityListing> listings) {
    outputAdapters.parallelStream().forEach(n -> n.publishAsynchronously(listings));
  }
}
