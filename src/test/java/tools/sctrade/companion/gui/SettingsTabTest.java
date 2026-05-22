package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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

  @Test
  void whenBuildingSettingsTabThenHelpIconsPresentForTooltipFields() {
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

    Set<Node> helpIcons = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      SettingsTab settingsTab = new SettingsTab(userService, gameLogPathSubject, settings);
      return settingsTab.lookupAll(".settings-help-icon");
    });

    assertFalse(helpIcons.isEmpty());
  }

  @Test
  void whenBuildingSettingsTabThenNoDisabledHintLabels() {
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

    boolean anyDisabledLabel = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      SettingsTab settingsTab = new SettingsTab(userService, gameLogPathSubject, settings);
      return settingsTab.lookupAll(".label").stream().filter(Label.class::isInstance)
          .map(Label.class::cast).anyMatch(Node::isDisabled);
    });

    assertFalse(anyDisabledLabel);
  }

  @Test
  void whenBuildingSettingsTabThenHelpIconCountMatchesTooltipFields() {
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

    int helpIconCount = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      SettingsTab settingsTab = new SettingsTab(userService, gameLogPathSubject, settings);
      return settingsTab.lookupAll(".settings-help-icon").size();
    });

    // username, commodity keybind, item keybind, star citizen live path = 4 fields with tooltips
    assertEquals(4, helpIconCount);
  }

  @Test
  void whenBuildingSettingsTabThenHelpIconHasTooltip() {
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

    boolean allHelpIconsHaveTooltip = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      SettingsTab settingsTab = new SettingsTab(userService, gameLogPathSubject, settings);
      return settingsTab.lookupAll(".settings-help-icon").stream().filter(Label.class::isInstance)
          .map(Label.class::cast).allMatch(icon -> {
            Tooltip tooltip = icon.getTooltip();
            return tooltip != null && !tooltip.getText().isBlank()
                && tooltip.getShowDelay().toMillis() <= 100.0;
          });
    });

    assertTrue(allHelpIconsHaveTooltip);
  }

  @Test
  void whenBuildingSettingsTabThenUseTopAndBottomSpacersToCenterFormVertically() {
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

    var vgrowValues = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      SettingsTab settingsTab = new SettingsTab(userService, gameLogPathSubject, settings);
      Node topSpacer = settingsTab.lookup("#settingsTopSpacer");
      Node bottomSpacer = settingsTab.lookup("#settingsBottomSpacer");
      return new Object[] {topSpacer != null, bottomSpacer != null,
          topSpacer == null ? null : GridPane.getVgrow(topSpacer),
          bottomSpacer == null ? null : GridPane.getVgrow(bottomSpacer)};
    });

    assertTrue((Boolean) vgrowValues[0], "Top spacer should exist");
    assertTrue((Boolean) vgrowValues[1], "Bottom spacer should exist");
    assertEquals(Priority.ALWAYS, vgrowValues[2], "Top spacer should absorb extra height");
    assertEquals(Priority.ALWAYS, vgrowValues[3], "Bottom spacer should absorb extra height");
  }
}
