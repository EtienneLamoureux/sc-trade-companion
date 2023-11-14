package tools.sctrade.companion.output.commodity;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tools.sctrade.companion.domain.commodity.CommoditySubmission;
import tools.sctrade.companion.domain.user.Setting;
import tools.sctrade.companion.domain.user.SettingRepository;
import tools.sctrade.companion.utils.AsynchronousProcessor;

public class ScTradeToolsClient extends AsynchronousProcessor<CommoditySubmission> {
  private final Logger logger = LoggerFactory.getLogger(ScTradeToolsClient.class);

  private WebClient webClient;

  public ScTradeToolsClient(WebClient.Builder webClientBuilder, SettingRepository settings) {
    this.webClient =
        webClientBuilder.baseUrl(settings.get(Setting.SC_TRADE_TOOLS_ROOT_URL)).build();
  }

  @Override
  public void process(CommoditySubmission submission) {
    logger.debug("Sending {} commodity listings to SC Trade Tools...",
        submission.getListings().size());
    var response = webClient.post().uri("/api/crowdsource/commodity-listings")
        .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(buildDto(submission)))
        .header("signature", "").retrieve().toBodilessEntity();
    response.block();
    logger.info("Sent {} commodity listings to SC Trade Tools", submission.getListings().size());
  }

  private CommoditySubmissionDto buildDto(CommoditySubmission submission) {
    UserDto userDto = new UserDto(submission.getUser().id(), submission.getUser().label());
    List<CommodityListingDto> listings = submission.getListings().parallelStream()
        .map(n -> new CommodityListingDto(n.location(), n.transactionType().toString(),
            n.commodity(), n.price(), n.inventory(), n.inventoryLevel().getSaturation(),
            n.batchId(), new Timestamp(n.timestamp().toEpochMilli())))
        .toList();

    return new CommoditySubmissionDto(userDto, listings);
  }

  private record CommoditySubmissionDto(UserDto user, Collection<CommodityListingDto> listings) {
  }

  private record UserDto(String id, String label) {
  }

  private record CommodityListingDto(String location, String transaction, String commodity,
      double price, Integer quantity, double saturation, String batchId, Timestamp timestamp) {
  }
}
