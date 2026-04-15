package tools.sctrade.companion.output.commodity;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.sctrade.companion.domain.commodity.CommodityRepository;
import tools.sctrade.companion.domain.commodity.CommoditySubmission;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.exceptions.PublicationException;
import tools.sctrade.companion.output.ScTradeToolsClient;
import tools.sctrade.companion.utils.AsynchronousProcessor;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * sc-trade.tools implementation of {@link CommodityRepository} and asynchronous publisher of
 * commodity submissions.
 */
public class ScTradeToolsCommodityPublisher extends AsynchronousProcessor<CommoditySubmission>
    implements CommodityRepository {
  private final Logger logger = LoggerFactory.getLogger(ScTradeToolsCommodityPublisher.class);
  private final WebClient webClient;

  /**
   * Creates a new instance of {@link ScTradeToolsCommodityPublisher}.
   *
   * @param client The sc-trade.tools HTTP client.
   * @param notificationService The notification service.
   */
  public ScTradeToolsCommodityPublisher(ScTradeToolsClient client,
      NotificationService notificationService) {
    super(notificationService);
    this.webClient = client.getWebClient();
  }

  @Override
  @Cacheable("ScTradeToolsCommodityPublisher.findAllCommodities")
  public List<String> findAllCommodities() {
    logger.debug("Fetching commodities from sc-trade.tools...");
    return Arrays
        .stream(
            webClient.get().uri("/api/commodity/items").retrieve().bodyToMono(CommodityDto[].class)
                .retryWhen(ScTradeToolsClient.onTransientNetworkError()).block())
        .map(n -> n.name().toLowerCase(Locale.ROOT)).toList();
  }

  @Override
  public void process(CommoditySubmission submission) {
    logger.debug("Sending {} commodity listings to SC Trade Tools...",
        submission.getListings().size());

    try {
      var response = webClient.post().uri("/api/crowdsource/commodity-listings")
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(buildDto(submission))).header("signature", "").retrieve()
          .toBodilessEntity().retryWhen(ScTradeToolsClient.onTransientNetworkError());
      response.block();
      logger.info("Sent {} commodity listings to SC Trade Tools", submission.getListings().size());
      notificationService.info(String.format(Locale.ROOT,
          LocalizationUtil.get("infoCommodityListingsScTradeToolsOutput"),
          submission.getListings().size()));
    } catch (WebClientResponseException e) {
      throw new PublicationException(e.getResponseBodyAsString());
    }
  }

  private CommoditySubmissionDto buildDto(CommoditySubmission submission) {
    UserDto userDto = new UserDto(submission.getUser().id(), submission.getUser().label());
    List<CommodityListingDto> listings = submission.getListings().parallelStream()
        .map(n -> new CommodityListingDto(n.location(), n.transactionType().toString(),
            n.commodity(), n.price(), n.inventory(),
            (n.inventoryLevel() == null ? null : n.inventoryLevel().getSaturation()),
            n.boxSizesInScu(), n.batchId(), new Timestamp(n.timestamp().toEpochMilli())))
        .toList();

    return new CommoditySubmissionDto(userDto, listings);
  }

  private record CommodityDto(String name) {
  }

  private record CommoditySubmissionDto(UserDto user, Collection<CommodityListingDto> listings) {
  }

  private record UserDto(String id, String label) {
  }

  private record CommodityListingDto(String location, String transaction, String commodity,
      Double price, Integer quantity, Double saturation, List<Integer> availableBoxSizesInScu,
      String batchId, Timestamp timestamp) {
  }
}
