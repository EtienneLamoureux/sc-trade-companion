package tools.sctrade.companion.output.commodity;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.logging.AdvancedByteBufFormat;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.commodity.CommodityRepository;
import tools.sctrade.companion.domain.commodity.CommoditySubmission;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.exceptions.PublicationException;
import tools.sctrade.companion.utils.AsynchronousProcessor;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Asynchronous processor to send commodity listings to sc-trade.tools.
 */
public class ScTradeToolsClient extends AsynchronousProcessor<CommoditySubmission>
    implements CommodityRepository, LocationRepository {
  private final Logger logger = LoggerFactory.getLogger(ScTradeToolsClient.class);

  private WebClient webClient;

  /**
   * Creates a new instance of the sc-trade.tools client.
   *
   * @param webClientBuilder The web client builder.
   * @param settings The settings repository.
   * @param notificationService The notification service.
   * @param version The version of this application (we don't want an submissions from an out of
   *        date application).
   */
  public ScTradeToolsClient(WebClient.Builder webClientBuilder, SettingRepository settings,
      NotificationService notificationService, String version) {
    super(notificationService);

    
    /** 
     * Original Http Client
     * HttpClient httpClient = HttpClient.create(); httpClient =
     * httpClient.resolver(nameResolverSpec -> nameResolverSpec.retryTcpOnTimeout(true));
     * ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
     * 
     * this.webClient = webClientBuilder.baseUrl(settings.get(Setting.SC_TRADE_TOOLS_ROOT_URL))
     * .clientConnector(connector).defaultHeader("x-companion-version", version).build();
     */

    ConnectionProvider provider = ConnectionProvider.builder("sc-trade").maxConnections(50)
        .pendingAcquireTimeout(Duration.ofSeconds(10)).build();

    HttpClient httpClient = HttpClient.create(provider)
        .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG,
            AdvancedByteBufFormat.TEXTUAL)
        .responseTimeout(Duration.ofSeconds(10)).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .protocol(HttpProtocol.HTTP11);


    this.webClient = webClientBuilder.baseUrl(settings.get(Setting.SC_TRADE_TOOLS_ROOT_URL))
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader("x-companion-version", version)
        .defaultHeader(HttpHeaders.USER_AGENT, "sc-companion/" + version)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).build();
  }

  @Override
  @Cacheable("ScTradeToolsClient.findAllCommodities")
  public List<String> findAllCommodities() {
    logger.debug("Fetching commodities from sc-trade.tools...");
    return Arrays
        .stream(
            this.webClient.get().uri("/api/items").retrieve().bodyToMono(String[].class).block())
        .map(n -> n.toLowerCase(Locale.ROOT)).toList();
  }

  @Override
  @Cacheable("ScTradeToolsClient.findAllLocations")
  public Collection<String> findAllLocations() {
    logger.debug("Fetching locations from sc-trade.tools...");
    LocationDto[] locationDtos = this.webClient.get().uri("/api/locations").retrieve()
        .bodyToMono(LocationDto[].class).block();
    return Arrays.stream(locationDtos).filter(n -> {
      String type = n.type;

      if (Arrays.asList("Lagrangian point", "Asteroid field", "System").contains(type)) {
        return false;
      }

      String name = n.name;
      boolean isParentLocation = Arrays.stream(locationDtos).map(m -> m.name())
          .filter(m -> !m.equals(name)).anyMatch(m -> m.contains(name));

      if (Arrays.asList("Moon surface", "Planet surface").contains(type) && isParentLocation) {
        return false;
      }

      return true;
    }).map(n -> n.name())
        .map(n -> n.substring(n.lastIndexOf(">") + 1).strip().toLowerCase(Locale.ROOT))
        .collect(Collectors.toSet());
  }

  @Override
  public void process(CommoditySubmission submission) {
    logger.debug("Sending {} commodity listings to SC Trade Tools...",
        submission.getListings().size());

    try {
      var response = webClient.post().uri("/api/crowdsource/commodity-listings")
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(buildDto(submission))).header("signature", "").retrieve()
          .toBodilessEntity();
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

  private record CommoditySubmissionDto(UserDto user, Collection<CommodityListingDto> listings) {
  }

  private record UserDto(String id, String label) {
  }

  private record CommodityListingDto(String location, String transaction, String commodity,
      Double price, Integer quantity, Double saturation, List<Integer> availableBoxSizesInScu,
      String batchId, Timestamp timestamp) {
  }

  private record LocationDto(String name, String type) {
  }
}
