package tools.sctrade.companion.output.item;

import java.util.Arrays;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.reactive.function.client.WebClient;
import tools.sctrade.companion.domain.item.ItemShopRepository;
import tools.sctrade.companion.output.ScTradeToolsClient;

/**
 * sc-trade.tools implementation of {@link ItemShopRepository}.
 */
public class ScTradeToolsItemShopRepository implements ItemShopRepository {
  private final Logger logger = LoggerFactory.getLogger(ScTradeToolsItemShopRepository.class);
  private final WebClient webClient;

  /**
   * Creates a new instance of {@link ScTradeToolsItemShopRepository}.
   *
   * @param client The sc-trade.tools HTTP client.
   */
  public ScTradeToolsItemShopRepository(ScTradeToolsClient client) {
    this.webClient = client.getWebClient();
  }

  @Override
  @Cacheable("ScTradeToolsItemShopRepository.findAllTypes")
  public Collection<String> findAllTypes() {
    logger.debug("Fetching item shop types from sc-trade.tools...");
    String[] types = webClient.get().uri("/api/items/shop-types").retrieve()
        .bodyToMono(String[].class).retryWhen(ScTradeToolsClient.onTransientNetworkError()).block();
    logger.debug("Fetched {} item shop types from sc-trade.tools", types.length);
    return Arrays.asList(types);
  }
}
