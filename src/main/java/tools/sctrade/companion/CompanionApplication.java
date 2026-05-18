package tools.sctrade.companion;

import javafx.application.Application;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.sctrade.companion.input.KeyListener;

@SpringBootApplication
@EnableScheduling
public class CompanionApplication {
  private static final Logger logger = LoggerFactory.getLogger(CompanionApplication.class);

  public static void main(String[] args) {
    var context = new SpringApplicationBuilder(CompanionApplication.class).headless(false)
        .web(WebApplicationType.NONE).run(args);

    registerKeyListenerOrCrash(context);
    openGui(context, args);
  }

  private static void registerKeyListenerOrCrash(ConfigurableApplicationContext context) {
    try {
      GlobalScreen.registerNativeHook();
    } catch (NativeHookException e) {
      logger.error("There was a problem registering the native hook", e);

      System.exit(1);
    }

    context.getBeansOfType(KeyListener.class).values().forEach(GlobalScreen::addNativeKeyListener);
  }

  private static void openGui(ConfigurableApplicationContext context, String[] args) {
    CompanionFxApplication.setApplicationContext(context);
    Application.launch(CompanionFxApplication.class, args);
  }
}
