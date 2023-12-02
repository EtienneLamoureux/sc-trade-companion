package tools.sctrade.companion.domain;

import java.util.List;

public interface LocationRepository {
  List<String> findAllLocations();
}
