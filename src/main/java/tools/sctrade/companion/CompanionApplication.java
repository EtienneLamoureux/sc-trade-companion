package tools.sctrade.companion;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import java.awt.AWTException;
import java.awt.EventQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class CompanionApplication {
  @Autowired
  public CompanionApplication(CompanionGui gui) throws AWTException {
    gui.initialize();
  }

  public static void main(String[] args) {
    var context = new SpringApplicationBuilder(CompanionApplication.class).headless(false)
        .web(WebApplicationType.NONE).run(args);

    try {
      GlobalScreen.registerNativeHook();
    } catch (NativeHookException ex) {
      System.err.println("There was a problem registering the native hook.");
      System.err.println(ex.getMessage());

      System.exit(1);
    }

    GlobalScreen.addNativeKeyListener(new KeyListener());

    openGui(context);
  }

  private static void openGui(ConfigurableApplicationContext context) {
    EventQueue.invokeLater(() -> {
      var ex = context.getBean(CompanionGui.class);
      ex.setVisible(true);
    });
  }
}
