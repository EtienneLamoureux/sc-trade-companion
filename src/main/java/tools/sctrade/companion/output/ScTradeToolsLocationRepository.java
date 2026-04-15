package tools.sctrade.companion.output;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.reactive.function.client.WebClient;
import tools.sctrade.companion.domain.LocationRepository;

/**
 * sc-trade.tools implementation of {@link LocationRepository}.
 */
public class ScTradeToolsLocationRepository implements LocationRepository {
  private final Logger logger = LoggerFactory.getLogger(ScTradeToolsLocationRepository.class);
  private final WebClient webClient;

  /**
   * Creates a new instance of {@link ScTradeToolsLocationRepository}.
   *
   * @param client The sc-trade.tools HTTP client.
   */
  public ScTradeToolsLocationRepository(ScTradeToolsClient client) {
    this.webClient = client.getWebClient();
  }

  @Override
  @Cacheable("ScTradeToolsLocationRepository.findAllLocations")
  public Collection<String> findAllLocations() {
    logger.debug("Fetching locations from sc-trade.tools...");
    LocationDto[] locationDtos =
        webClient.get().uri("/api/locations").retrieve().bodyToMono(LocationDto[].class)
            .retryWhen(ScTradeToolsClient.onTransientNetworkError()).block();
    return Arrays.stream(locationDtos).filter(n -> {
      String type = n.type();

      if (Arrays.asList("Lagrangian point", "Asteroid field", "System").contains(type)) {
        return false;
      }

      String name = n.name();
      boolean isParentLocation = Arrays.stream(locationDtos).map(LocationDto::name)
          .filter(m -> !m.equals(name)).anyMatch(m -> m.contains(name));

      if (Arrays.asList("Moon surface", "Planet surface").contains(type) && isParentLocation) {
        return false;
      }

      return true;
    }).map(LocationDto::name)
        .map(n -> n.substring(n.lastIndexOf(">") + 1).strip().toLowerCase(Locale.ROOT))
        .collect(Collectors.toSet());
  }

  private record LocationDto(String name, String type) {
  }
}
