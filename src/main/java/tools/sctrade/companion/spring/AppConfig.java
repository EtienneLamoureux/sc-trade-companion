package tools.sctrade.companion.spring;

import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.sctrade.companion.CompanionGui;
import tools.sctrade.companion.KeyListener;

@Configuration
public class AppConfig {
  @Bean
  public CompanionGui buildCompanionGui() {
    return new CompanionGui();
  }

  @Bean
  public NativeKeyListener buildNativeKeyListener() {
    return new KeyListener();
  }
}
