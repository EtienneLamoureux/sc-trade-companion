package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Decorator around multiple {@link CommodityLocationReader}. Runs each reader and keeps the result
 * of the first one that returns a non-empty location.
 */
public class BestEffortCommodityLocationReader extends CommodityLocationReader {
  private Collection<CommodityLocationReader> commodityLocationReaders;

  /**
   * Constructor for {@link BestEffortCommodityLocationReader}.
   *
   * @param commodityLocationReaders List of differently configured readers.
   */
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
