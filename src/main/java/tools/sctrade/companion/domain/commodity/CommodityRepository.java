package tools.sctrade.companion.domain.commodity;

import java.util.List;

/**
 * Repository for commodities.
 */
public interface CommodityRepository {
  /**
   * Returns a list of all commodities' names.
   *
   * @return List of commodities' names.
   */
  List<String> findAllCommodities();
}
