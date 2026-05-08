package tools.sctrade.companion.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import tools.sctrade.companion.CompanionApplication;

class CompanionApplicationContextTest {
  @Test
  void givenJavaFxViewsWhenCreatingSpringContextThenContextStartsBeforeToolkitInitialization() {
    assertDoesNotThrow(() -> {
      try (ConfigurableApplicationContext context =
          new SpringApplicationBuilder(CompanionApplication.class).headless(false)
              .web(WebApplicationType.NONE).run("--spring.main.banner-mode=off")) {
      }
    });
  }
}
