package tools.sctrade.companion.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.sctrade.companion.CompanionGui;

@Configuration
public class AppConfig {
  @Bean
  public CompanionGui buildCompanionGui() {
    return new CompanionGui();
  }
}
