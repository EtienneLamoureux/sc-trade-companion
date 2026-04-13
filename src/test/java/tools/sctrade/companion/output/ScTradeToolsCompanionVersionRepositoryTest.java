package tools.sctrade.companion.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ScTradeToolsCompanionVersionRepositoryTest {
  private static final String LATEST_VERSION_ENDPOINT =
      "/api/crowdsource/companion-versions/latest";
  private static final String VERSION = "1.2.3";

  @Mock
  private ScTradeToolsClient client;
  @Mock
  private WebClient webClient;
  @Mock
  private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
  @Mock
  private WebClient.ResponseSpec responseSpec;

  private ScTradeToolsCompanionVersionRepository repository;

  @BeforeEach
  void setUp() {
    when(client.getWebClient()).thenReturn(webClient);
    repository = new ScTradeToolsCompanionVersionRepository(client);
  }

  @Test
  void whenFetchingLatestVersionThenCallsCorrectEndpoint() {
    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(LATEST_VERSION_ENDPOINT);
    doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(VERSION));

    repository.fetchLatestVersion();

    verify(requestHeadersUriSpec).uri(LATEST_VERSION_ENDPOINT);
  }

  @Test
  void whenFetchingLatestVersionThenReturnsResponseBody() {
    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(LATEST_VERSION_ENDPOINT);
    doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(VERSION));

    String result = repository.fetchLatestVersion();

    assertEquals(VERSION, result);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t", "\n"})
  void givenNullOrBlankBodyWhenFetchingLatestVersionThenThrows(String body) {
    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(LATEST_VERSION_ENDPOINT);
    doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
    Mono<String> mono = body == null ? Mono.empty() : Mono.just(body);
    when(responseSpec.bodyToMono(String.class)).thenReturn(mono);

    assertThrows(IllegalStateException.class, () -> repository.fetchLatestVersion());
  }
}
