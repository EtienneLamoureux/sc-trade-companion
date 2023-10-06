package tools.sctrade.companion;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.awt.AWTException;
import java.awt.EventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class CompanionApplication {
  private static final Logger logger = LoggerFactory.getLogger(CompanionApplication.class);

  @Autowired
  public CompanionApplication(CompanionGui gui) throws AWTException {
    gui.initialize();
  }

  public static void main(String[] args) {
    var context = new SpringApplicationBuilder(CompanionApplication.class).headless(false)
        .web(WebApplicationType.NONE).run(args);

    registerKeyListenerOrCrash(context);
    openGui(context);
  }

  private static void registerKeyListenerOrCrash(ConfigurableApplicationContext context) {
    try {
      GlobalScreen.registerNativeHook();
    } catch (NativeHookException ex) {
      logger.error("There was a problem registering the native hook.", ex);

      System.exit(1);
    }

    GlobalScreen.addNativeKeyListener(context.getBean(NativeKeyListener.class));
  }

  private static void openGui(ConfigurableApplicationContext context) {
    EventQueue.invokeLater(() -> {
      var ex = context.getBean(CompanionGui.class);
      ex.setVisible(true);
    });
  }
}
