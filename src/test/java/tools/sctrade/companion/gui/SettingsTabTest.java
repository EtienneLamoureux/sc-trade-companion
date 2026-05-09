package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Optional;
import javafx.scene.control.TextField;
import org.jnativehook.keyboard.NativeKeyEvent;
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

    SettingRepository settings = mock(SettingRepository.class);
    when(settings.get(Setting.MY_DATA_PATH)).thenReturn(Path.of("my-data"));
    when(settings.get(Setting.MY_IMAGES_PATH)).thenReturn(Path.of("my-images"));
    when(settings.get(Setting.PRINTSCREEN_COMMODITY_KEYBIND, NativeKeyEvent.VC_F3))
        .thenReturn(NativeKeyEvent.VC_F3);
    when(settings.get(Setting.PRINTSCREEN_ITEM_KEYBIND, NativeKeyEvent.VC_F3))
        .thenReturn(NativeKeyEvent.VC_F3);
    when(settings.get(eq(Setting.STAR_CITIZEN_MONITOR), anyString()))
        .thenAnswer(invocation -> invocation.getArgument(1));

    TextField itemKeybindField = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      SettingsTab settingsTab = new SettingsTab(userService, gameLogPathSubject, settings);
      return (TextField) settingsTab.lookup("#itemKeybindField");
    });

    assertEquals("F3", itemKeybindField.getText());
  }
}
