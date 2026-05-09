package tools.sctrade.companion;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import tools.sctrade.companion.gui.CompanionGui;
import tools.sctrade.companion.gui.CompanionTheme;
import tools.sctrade.companion.gui.version.CompanionVersionChecker;

/**
 * JavaFX bootstrap application for SC Trade Companion.
 */
public class CompanionFxApplication extends Application {
  private static ConfigurableApplicationContext applicationContext;

  /**
   * Stores the Spring application context used by the JavaFX bootstrap.
   *
   * @param context the Spring application context
   */
  public static void setApplicationContext(ConfigurableApplicationContext context) {
    applicationContext = context;
  }

  /**
   * Starts the JavaFX UI using the Spring-managed GUI bean.
   *
   * @param primaryStage the primary JavaFX stage
   */
  @Override
  public void start(Stage primaryStage) {
    CompanionTheme.applyUserAgentStylesheet();
    var gui = applicationContext.getBean(CompanionGui.class);
    gui.initialize(primaryStage);
    primaryStage.show();
    applicationContext.getBean(CompanionVersionChecker.class).checkAsynchronously();
  }
}
