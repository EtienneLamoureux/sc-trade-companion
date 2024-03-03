package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class BestEffortCommodityLocationReader extends CommodityLocationReader {
  private Collection<CommodityLocationReader> commodityLocationReaders;

  public BestEffortCommodityLocationReader(
      Collection<CommodityLocationReader> commodityLocationReaders) {
    super(Collections.emptyList(), null);
    this.commodityLocationReaders = commodityLocationReaders;
  }

  @Override
  public Optional<String> read(BufferedImage screenCapture) {
    return commodityLocationReaders.stream().map(n -> n.read(screenCapture))
        .filter(n -> n.isPresent()).map(n -> n.get()).findAny();
  }
}
