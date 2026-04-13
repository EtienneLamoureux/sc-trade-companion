package tools.sctrade.companion.domain.item;

import java.util.Collection;

public interface ItemShopRepository {
  Collection<String> findAllTypes();
}
