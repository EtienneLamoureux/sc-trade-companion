package tools.sctrade.companion.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import tools.sctrade.companion.CompanionApplication;
import tools.sctrade.companion.domain.commodity.CommoditySubmissionFactory;
import tools.sctrade.companion.domain.item.ItemSubmissionFactory;

class CompanionApplicationContextTest {
  @Test
  void givenJavaFxViewsWhenCreatingSpringContextThenContextStartsBeforeToolkitInitialization() {
    assertDoesNotThrow(() -> {
      try (ConfigurableApplicationContext context =
          new SpringApplicationBuilder(CompanionApplication.class, NativeOcrTestConfig.class)
              .headless(false).web(WebApplicationType.NONE)
              .properties("spring.main.allow-bean-definition-overriding=true")
              .run("--spring.main.banner-mode=off")) {
      }
    });
  }

  @TestConfiguration
  static class NativeOcrTestConfig {
    @Bean("RawCommoditySubmissionFactory")
    CommoditySubmissionFactory buildRawCommoditySubmissionFactory() {
      return mock(CommoditySubmissionFactory.class);
    }

    @Bean("RawItemSubmissionFactory")
    ItemSubmissionFactory buildRawItemSubmissionFactory() {
      return mock(ItemSubmissionFactory.class);
    }
  }
}
