package tools.sctrade.companion.output;

import java.net.SocketException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

class ScTradeToolsClientTest {
  private static final int MAX_RETRIES = 3;

  @Test
  void givenWebClientRequestExceptionWhenRetryingThenRetriesUpToMaxAttempts() {
    AtomicInteger attempts = new AtomicInteger(0);
    Retry retrySpec = ScTradeToolsClient.onTransientNetworkError();

    Mono<String> mono = Mono.defer(() -> {
      attempts.incrementAndGet();
      return Mono.<String>error(buildRequestException(new SocketException("Connection reset")));
    }).retryWhen(retrySpec);

    StepVerifier.create(mono).expectError(WebClientRequestException.class).verify();

    // 1 initial attempt + 3 retries
    assert attempts.get() == MAX_RETRIES + 1;
  }

  @Test
  void givenWebClientRequestExceptionWhenRetriesExhaustedThenRethrowsOriginalException() {
    Retry retrySpec = ScTradeToolsClient.onTransientNetworkError();
    WebClientRequestException originalException =
        buildRequestException(new SocketException("Connection reset"));

    Mono<String> mono = Mono.<String>error(originalException).retryWhen(retrySpec);

    StepVerifier.create(mono).expectErrorMatches(e -> e == originalException).verify();
  }

  @Test
  void givenWebClientResponseExceptionWhenRetryingThenDoesNotRetry() {
    AtomicInteger attempts = new AtomicInteger(0);
    Retry retrySpec = ScTradeToolsClient.onTransientNetworkError();

    Mono<String> mono = Mono.defer(() -> {
      attempts.incrementAndGet();
      return Mono.<String>error(
          WebClientResponseException.create(503, "Service Unavailable", null, null, null));
    }).retryWhen(retrySpec);

    StepVerifier.create(mono).expectError(WebClientResponseException.class).verify();

    // No retries — only 1 attempt
    assert attempts.get() == 1;
  }

  @Test
  void givenWebClientResponseExceptionWhenRetryingThenErrorPropagatesImmediately() {
    Retry retrySpec = ScTradeToolsClient.onTransientNetworkError();
    WebClientResponseException responseException =
        WebClientResponseException.create(503, "Service Unavailable", null, null, null);

    Mono<String> mono = Mono.<String>error(responseException).retryWhen(retrySpec);

    StepVerifier.create(mono).expectError(WebClientResponseException.class).verify();
  }

  @Test
  void givenSuccessAfterOneTransientFailureWhenRetryingThenSucceeds() {
    AtomicInteger attempts = new AtomicInteger(0);
    Retry retrySpec = ScTradeToolsClient.onTransientNetworkError();

    Mono<String> mono = Mono.defer(() -> {
      if (attempts.incrementAndGet() < 2) {
        return Mono.<String>error(buildRequestException(new SocketException("Connection reset")));
      }
      return Mono.just("ok");
    }).retryWhen(retrySpec);

    StepVerifier.create(mono).expectNext("ok").expectComplete().verify();
  }

  @Test
  void givenIllegalArgumentExceptionWhenRetryingThenDoesNotRetry() {
    AtomicInteger attempts = new AtomicInteger(0);
    Retry retrySpec = ScTradeToolsClient.onTransientNetworkError();

    Mono<String> mono = Mono.defer(() -> {
      attempts.incrementAndGet();
      return Mono.<String>error(new IllegalArgumentException("unexpected"));
    }).retryWhen(retrySpec);

    StepVerifier.create(mono).expectError(IllegalArgumentException.class).verify();

    assert attempts.get() == 1;
  }

  private static WebClientRequestException buildRequestException(Throwable cause) {
    return new WebClientRequestException(cause, HttpMethod.POST,
        URI.create("https://sc-trade.tools/api/crowdsource/commodity-listings"), HttpHeaders.EMPTY);
  }
}
