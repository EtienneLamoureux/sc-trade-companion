package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Optional;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.gamelog.GameLogPathSubject;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.User;
import tools.sctrade.companion.domain.user.UserService;

class SettingsTabTest {
  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenItemKeybindUnsetWhenBuildingSettingsTabThenDisplayF3() {
    UserService userService = mock(UserService.class);
    when(userService.get()).thenReturn(new User("id", "Pilot"));
    GameLogPathSubject gameLogPathSubject = mock(GameLogPathSubject.class);
    when(gameLogPathSubject.getStarCitizenLivePath()).thenReturn(Optional.of("LIVE"));

    SettingRepository settings = new SettingRepository();
    settings.set(Setting.MY_DATA_PATH, Path.of("my-data"));
    settings.set(Setting.MY_IMAGES_PATH, Path.of("my-images"));

    TextField itemKeybindField = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      SettingsTab settingsTab = new SettingsTab(userService, gameLogPathSubject, settings);
      return (TextField) settingsTab.lookup("#itemKeybindField");
    });

    assertEquals("F3", itemKeybindField.getText());
  }
}
