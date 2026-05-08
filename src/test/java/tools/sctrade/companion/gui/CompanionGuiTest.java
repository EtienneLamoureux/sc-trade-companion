package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.gamelog.GameLogPathSubject;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.User;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.utils.LocalizationUtil;

class CompanionGuiTest {
  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenGuiWhenInitializedThenBuildCurrentShellAsJavaFxStage() {
    UserService userService = mock(UserService.class);
    when(userService.get()).thenReturn(new User("id", "Pilot"));
    GameLogPathSubject gameLogPathSubject = mock(GameLogPathSubject.class);
    when(gameLogPathSubject.getStarCitizenLivePath()).thenReturn(Optional.of("LIVE"));
    SettingRepository settings = new SettingRepository();
    settings.set(Setting.MY_DATA_PATH, Path.of("my-data"));
    settings.set(Setting.MY_IMAGES_PATH, Path.of("my-images"));

    Stage stage = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      Stage fxStage = new Stage();
      CompanionGui gui = new CompanionGui(userService, gameLogPathSubject, settings, "1.5.2");
      gui.initialize(fxStage);
      return fxStage;
    });

    assertEquals("SC Trade Companion 1.5.2", stage.getTitle());
    assertEquals(600.0, stage.getWidth());
    assertEquals(575.0, stage.getHeight());

    Scene scene = stage.getScene();
    VBox root = assertInstanceOf(VBox.class, scene.getRoot());
    assertInstanceOf(MenuBar.class, root.getChildren().get(0));

    BorderPane content = assertInstanceOf(BorderPane.class, root.getChildren().get(1));
    TabPane tabPane = assertInstanceOf(TabPane.class, content.getCenter());
    assertEquals(LocalizationUtil.get("tabUsage"), tabPane.getTabs().get(0).getText());
    assertEquals(LocalizationUtil.get("tabSettings"), tabPane.getTabs().get(1).getText());
    assertEquals(LocalizationUtil.get("tabLogs"), tabPane.getTabs().get(2).getText());
  }
}
