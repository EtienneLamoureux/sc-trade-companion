package tools.sctrade.companion.output;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
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
    HttpClient httpClient = HttpClient.create();
    httpClient = httpClient.resolver(nameResolverSpec -> nameResolverSpec.retryTcpOnTimeout(true));
    ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

    this.webClient = webClientBuilder.baseUrl(settings.get(Setting.SC_TRADE_TOOLS_ROOT_URL))
        .clientConnector(connector).defaultHeader("x-companion-version", version).build();
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
