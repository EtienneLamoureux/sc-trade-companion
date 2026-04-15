package tools.sctrade.companion.output.item;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import tools.sctrade.companion.domain.item.ItemRepository;
import tools.sctrade.companion.output.ScTradeToolsClient;

/**
 * sc-trade.tools implementation of {@link ItemRepository}.
 */
public class ScTradeToolsItemRepository implements ItemRepository {
  private final Logger logger = LoggerFactory.getLogger(ScTradeToolsItemRepository.class);
  private final WebClient webClient;

  /**
   * Creates a new instance of {@link ScTradeToolsItemRepository}.
   *
   * @param client The sc-trade.tools HTTP client.
   */
  public ScTradeToolsItemRepository(ScTradeToolsClient client) {
    this.webClient = client.getWebClient();
  }

  @Override
  @Cacheable("ScTradeToolsItemRepository.findAllItems")
  public List<String> findAllItems() {
    logger.debug("Fetching items from sc-trade.tools...");

    ItemPageDto firstPage = fetchItemPage(0);
    int totalPages = firstPage.page().totalPages();
    logger.debug("sc-trade.tools reports {} pages of items", totalPages);

    List<String> firstPageNames = toItemNames(firstPage);

    if (totalPages <= 1) {
      return firstPageNames.stream().distinct().toList();
    }

    List<String> remainingNames =
        Flux.fromStream(java.util.stream.IntStream.rangeClosed(1, totalPages - 1).boxed())
            .parallel().runOn(Schedulers.boundedElastic())
            .flatMap(page -> Flux.fromIterable(toItemNames(fetchItemPage(page)))).sequential()
            .collectList().block();

    List<String> itemNames = Flux.fromIterable(firstPageNames)
        .concatWith(Flux.fromIterable(remainingNames)).distinct().collectList().block();
    logger.debug("Fetched {} items from sc-trade.tools", itemNames.size());

    return itemNames;
  }

  private ItemPageDto fetchItemPage(int pageNumber) {
    logger.debug("Fetching items page {}", pageNumber);
    return webClient.get()
        .uri(u -> u.path("/api/item/items").queryParam("page", pageNumber).build()).retrieve()
        .bodyToMono(ItemPageDto.class).retryWhen(ScTradeToolsClient.onTransientNetworkError())
        .block();
  }

  private List<String> toItemNames(ItemPageDto page) {
    return Arrays.stream(page.content()).map(n -> n.name().toLowerCase(Locale.ROOT)).toList();
  }

  private record ItemDto(String name) {
  }

  private record ItemPageDto(ItemDto[] content, PageDto page) {
    public record PageDto(int totalPages) {
    }
  }
}
