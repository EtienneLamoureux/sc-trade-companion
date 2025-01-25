package tools.sctrade.companion.domain;

import java.util.Collection;

/**
 * Repository for locations. To be implemented by a concrete output port.
 */
public interface LocationRepository {
  Collection<String> findAllLocations();
}
