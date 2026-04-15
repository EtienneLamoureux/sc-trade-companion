package tools.sctrade.companion.output.item;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.sctrade.companion.domain.item.ItemListing;
import tools.sctrade.companion.domain.item.ItemSubmission;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.exceptions.PublicationException;
import tools.sctrade.companion.output.ScTradeToolsClient;
import tools.sctrade.companion.utils.AsynchronousProcessor;
import tools.sctrade.companion.utils.HashUtil;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * sc-trade.tools asynchronous publisher of item submissions.
 */
public class ScTradeToolsItemPublisher extends AsynchronousProcessor<ItemSubmission> {
  private final Logger logger = LoggerFactory.getLogger(ScTradeToolsItemPublisher.class);
  private final WebClient webClient;

  /**
   * Creates a new instance of {@link ScTradeToolsItemPublisher}.
   *
   * @param client The sc-trade.tools HTTP client.
   * @param notificationService The notification service.
   */
  public ScTradeToolsItemPublisher(ScTradeToolsClient client,
      NotificationService notificationService) {
    super(notificationService);
    this.webClient = client.getWebClient();
  }

  @Override
  public void process(ItemSubmission submission) {
    logger.debug("Sending {} item listings to SC Trade Tools...", submission.getListings().size());

    try {
      var response = webClient.post().uri("/api/crowdsource/item-listings")
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(buildDto(submission))).header("signature", "").retrieve()
          .toBodilessEntity().retryWhen(ScTradeToolsClient.onTransientNetworkError());
      response.block();
      logger.info("Sent {} item listings to SC Trade Tools", submission.getListings().size());
      notificationService.info(
          String.format(Locale.ROOT, LocalizationUtil.get("infoItemListingsScTradeToolsOutput"),
              submission.getListings().size()));
    } catch (WebClientResponseException e) {
      throw new PublicationException(e.getResponseBodyAsString());
    }
  }

  private ItemSubmissionDto buildDto(ItemSubmission submission) {
    UserDto userDto = new UserDto(submission.getUser().id(), submission.getUser().label());
    List<ItemListingDto> listings = submission.getListings().parallelStream()
        .map(n -> new ItemListingDto(n.location(), n.shop(), n.name(), n.price(), buildBatchId(n)))
        .toList();

    return new ItemSubmissionDto(userDto, listings);
  }

  private String buildBatchId(ItemListing listing) {
    return HashUtil.hash(String.format(Locale.ROOT, "%s%s%s%s%s", listing.location(),
        listing.shop(), listing.name(), listing.price(), Instant.now()));
  }

  private record ItemSubmissionDto(UserDto user, Collection<ItemListingDto> listings) {
  }

  private record UserDto(String id, String label) {
  }

  private record ItemListingDto(String location, String shop, String item, double price,
      String batchId) {
  }
}
