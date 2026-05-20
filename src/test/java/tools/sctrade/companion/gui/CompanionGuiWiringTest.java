package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Optional;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.gamelog.GameLogPathSubject;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.User;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.gui.screenshot.ScreenshotRepository;

/**
 * Tests for CompanionGui wiring with ScreenshotRepository dependency.
 */
class CompanionGuiWiringTest {
  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenCompanionGuiWhenConstructedThenAcceptsScreenshotRepository() {
    UserService userService = mock(UserService.class);
    when(userService.get()).thenReturn(new User("id", "Pilot"));
    GameLogPathSubject gameLogPathSubject = mock(GameLogPathSubject.class);
    when(gameLogPathSubject.getStarCitizenLivePath()).thenReturn(Optional.of("LIVE"));
    SettingRepository settings = new SettingRepository();
    settings.set(Setting.MY_DATA_PATH, Path.of("my-data"));
    settings.set(Setting.MY_IMAGES_PATH, Path.of("my-images"));
    ScreenshotRepository screenshotRepository = new ScreenshotRepository();

    CompanionGui gui = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> new CompanionGui(userService,
        gameLogPathSubject, settings, screenshotRepository, "1.5.2"));

    assertNotNull(gui, "CompanionGui should be created with ScreenshotRepository");
  }

  @Test
  void givenCompanionGuiWhenBuildSceneThenScreenshotsTabReceivesRepository() {
    UserService userService = mock(UserService.class);
    when(userService.get()).thenReturn(new User("id", "Pilot"));
    GameLogPathSubject gameLogPathSubject = mock(GameLogPathSubject.class);
    when(gameLogPathSubject.getStarCitizenLivePath()).thenReturn(Optional.of("LIVE"));
    SettingRepository settings = new SettingRepository();
    settings.set(Setting.MY_DATA_PATH, Path.of("my-data"));
    settings.set(Setting.MY_IMAGES_PATH, Path.of("my-images"));
    ScreenshotRepository screenshotRepository = new ScreenshotRepository();

    ScreenshotsTab screenshotsTab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      Stage fxStage = new Stage();
      CompanionGui gui = new CompanionGui(userService, gameLogPathSubject, settings,
          screenshotRepository, "1.5.2");
      gui.initialize(fxStage);

      StackPane stackRoot = (StackPane) fxStage.getScene().getRoot();
      BorderPane root = stackRoot.getChildren().stream().filter(BorderPane.class::isInstance)
          .map(BorderPane.class::cast).findFirst()
          .orElseThrow(() -> new AssertionError("Root not found"));

      javafx.scene.layout.HBox navBar = (javafx.scene.layout.HBox) root.getTop();
      navBar.getChildren().stream().filter(n -> "nav-screenshots".equals(n.getId()))
          .map(n -> (javafx.scene.control.Hyperlink) n).findFirst()
          .orElseThrow(() -> new AssertionError("Screenshots link not found")).fire();

      return (ScreenshotsTab) root.getCenter();
    });

    assertNotNull(screenshotsTab,
        "ScreenshotsTab should be displayed when screenshots nav is clicked");
  }

  @Test
  void givenCompanionGuiWhenInitializedThenAllTabsPresent() {
    UserService userService = mock(UserService.class);
    when(userService.get()).thenReturn(new User("id", "Pilot"));
    GameLogPathSubject gameLogPathSubject = mock(GameLogPathSubject.class);
    when(gameLogPathSubject.getStarCitizenLivePath()).thenReturn(Optional.of("LIVE"));
    SettingRepository settings = new SettingRepository();
    settings.set(Setting.MY_DATA_PATH, Path.of("my-data"));
    settings.set(Setting.MY_IMAGES_PATH, Path.of("my-images"));
    ScreenshotRepository screenshotRepository = new ScreenshotRepository();

    Stage stage = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      Stage fxStage = new Stage();
      CompanionGui gui = new CompanionGui(userService, gameLogPathSubject, settings,
          screenshotRepository, "1.5.2");
      gui.initialize(fxStage);
      return fxStage;
    });

    StackPane stackRoot = (StackPane) stage.getScene().getRoot();
    BorderPane root = stackRoot.getChildren().stream().filter(BorderPane.class::isInstance)
        .map(BorderPane.class::cast).findFirst()
        .orElseThrow(() -> new AssertionError("Root not found"));

    javafx.scene.layout.HBox navBar = (javafx.scene.layout.HBox) root.getTop();

    // Verify all 4 tabs are accessible via navigation
    assertNotNull(navBar.getChildren().stream().filter(n -> "nav-usage".equals(n.getId()))
        .findFirst().orElse(null), "Usage tab should be accessible");
    assertNotNull(navBar.getChildren().stream().filter(n -> "nav-settings".equals(n.getId()))
        .findFirst().orElse(null), "Settings tab should be accessible");
    assertNotNull(navBar.getChildren().stream().filter(n -> "nav-screenshots".equals(n.getId()))
        .findFirst().orElse(null), "Screenshots tab should be accessible");
    assertNotNull(navBar.getChildren().stream().filter(n -> "nav-logs".equals(n.getId()))
        .findFirst().orElse(null), "Logs tab should be accessible");
  }
}
