package tools.sctrade.companion.output;

import org.springframework.web.reactive.function.client.WebClient;

/**
 * sc-trade.tools adapter for fetching the latest published version of SC Trade Companion.
 */
public class ScTradeToolsCompanionVersionRepository {
  private final WebClient webClient;

  /**
   * Creates a new instance of {@link ScTradeToolsCompanionVersionRepository}.
   *
   * @param client The sc-trade.tools HTTP client.
   */
  public ScTradeToolsCompanionVersionRepository(ScTradeToolsClient client) {
    this.webClient = client.getWebClient();
  }

  /**
   * Fetches the latest available version string from sc-trade.tools.
   *
   * @return the latest version string (e.g. {@code "1.2.3"})
   */
  public String fetchLatestVersion() {
    return webClient.get().uri("/api/crowdsource/companion-versions/latest").retrieve()
        .bodyToMono(String.class).block();
  }
}
