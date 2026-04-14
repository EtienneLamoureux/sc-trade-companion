package tools.sctrade.companion.output;

import org.springframework.web.reactive.function.client.WebClient;
import tools.sctrade.companion.domain.CompanionVersionRepository;

/**
 * sc-trade.tools implementation of {@link CompanionVersionRepository}.
 */
public class ScTradeToolsCompanionVersionRepository implements CompanionVersionRepository {
  private final WebClient webClient;

  /**
   * Creates a new instance of {@link ScTradeToolsCompanionVersionRepository}.
   *
   * @param client The sc-trade.tools HTTP client.
   */
  public ScTradeToolsCompanionVersionRepository(ScTradeToolsClient client) {
    this.webClient = client.getWebClient();
  }

  @Override
  public String fetchLatestVersion() {
    String version = webClient.get().uri("/api/crowdsource/companion-versions/latest").retrieve()
        .bodyToMono(String.class).block();

    if (version == null || version.isBlank()) {
      throw new IllegalStateException("Received null or blank latest-version response");
    }

    return version.strip();
  }
}
