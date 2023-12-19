package tools.sctrade.companion.domain;

import java.util.Collection;

public interface LocationRepository {
  Collection<String> findAllLocations();
}
