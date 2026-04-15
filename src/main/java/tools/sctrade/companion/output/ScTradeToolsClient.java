package tools.sctrade.companion.output;

import java.time.Duration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;

/**
 * Provides a shared {@link WebClient} configured for the sc-trade.tools API. Intended to be
 * injected into client components via composition.
 */
public class ScTradeToolsClient {
  private final WebClient webClient;

  /**
   * Creates a new instance of {@link ScTradeToolsClient}.
   *
   * @param webClientBuilder The web client builder.
   * @param settings The settings repository.
   * @param version The version of this application (we don't want submissions from an out of date
   *        application).
   */
  public ScTradeToolsClient(WebClient.Builder webClientBuilder, SettingRepository settings,
      String version) {
    HttpClient httpClient = HttpClient.create(buildConnectionProvider());
    httpClient = httpClient.resolver(nameResolverSpec -> nameResolverSpec.retryTcpOnTimeout(true));
    ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

    this.webClient = webClientBuilder.baseUrl(settings.get(Setting.SC_TRADE_TOOLS_ROOT_URL))
        .clientConnector(connector).defaultHeader("x-companion-version", version).build();
  }

  /**
   * Builds a {@link ConnectionProvider} configured to tolerate Cloudflare (and other proxies) that
   * silently reset idle keep-alive connections. Connections idle for more than 20 s or alive for
   * more than 55 s are evicted, and a background task runs every 30 s to remove stale entries
   * proactively.
   *
   * @return the connection provider
   */
  private static ConnectionProvider buildConnectionProvider() {
    return ConnectionProvider.builder("sc-trade-tools").maxIdleTime(Duration.ofSeconds(20))
        .maxLifeTime(Duration.ofSeconds(55)).evictInBackground(Duration.ofSeconds(30)).build();
  }

  /**
   * Returns a {@link Retry} spec that retries up to 3 times on transient network errors. These are
   * transport-layer failures represented by {@link WebClientRequestException} (e.g. connection
   * reset, connection refused, timeout), as opposed to
   * {@link org.springframework.web.reactive.function.client.WebClientResponseException
   * WebClientResponseException} which signals a real HTTP 4xx/5xx response from the server and
   * should never be retried automatically.
   *
   * @return the retry spec
   */
  public static Retry onTransientNetworkError() {
    return Retry.backoff(3, Duration.ofMillis(500))
        .filter(t -> t instanceof WebClientRequestException)
        .onRetryExhaustedThrow((spec, signal) -> signal.failure());
  }

  /**
   * Returns the configured {@link WebClient}.
   *
   * @return the web client
   */
  public WebClient getWebClient() {
    return webClient;
  }
}
