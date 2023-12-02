package tools.sctrade.companion.domain.commodity;

import java.util.List;

public interface CommodityRepository {
  List<String> findAllCommodities();
}
