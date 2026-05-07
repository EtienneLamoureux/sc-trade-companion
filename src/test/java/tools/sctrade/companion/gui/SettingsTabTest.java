package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.gamelog.GameLogPathSubject;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.User;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.utils.LocalizationUtil;

class SettingsTabTest {
  @Test
  void givenItemKeybindUnsetWhenBuildingSettingsTabThenDisplayF5() {
    UserService userService = mock(UserService.class);
    when(userService.get()).thenReturn(new User("id", "Pilot"));
    GameLogPathSubject gameLogPathSubject = mock(GameLogPathSubject.class);
    when(gameLogPathSubject.getStarCitizenLivePath()).thenReturn(Optional.of("LIVE"));

    SettingRepository settings = new SettingRepository();
    settings.set(Setting.MY_DATA_PATH, Path.of("my-data"));
    settings.set(Setting.MY_IMAGES_PATH, Path.of("my-images"));
    SettingsTab settingsTab = new SettingsTab(userService, gameLogPathSubject, settings);

    assertEquals("F5",
        findKeybindFieldText(settingsTab, LocalizationUtil.get("labelPrintscreenItemKeybind")));
  }

  private String findKeybindFieldText(Container container, String labelText) {
    for (Component component : container.getComponents()) {
      if (component instanceof JLabel label && labelText.equals(label.getText())
          && label.getLabelFor() instanceof JPanel keybindPanel) {
        for (Component child : keybindPanel.getComponents()) {
          if (child instanceof JTextField textField) {
            return textField.getText();
          }
        }
      }

      if (component instanceof Container child) {
        String text = findKeybindFieldText(child, labelText);
        if (text != null) {
          return text;
        }
      }
    }

    return null;
  }
}
