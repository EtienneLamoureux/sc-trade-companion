package tools.sctrade.companion.spring;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.input.ScreenPrinter;

class AppConfigTest {
  @Test
  void givenItemKeybindUnsetWhenPressingF5ThenRunItemScreenPrinter() {
    SettingRepository settings = mock(SettingRepository.class);
    when(settings.get(eq(Setting.PRINTSCREEN_ITEM_KEYBIND), anyInt()))
        .thenAnswer(invocation -> invocation.getArgument(1));
    ScreenPrinter screenPrinter = mock(ScreenPrinter.class);
    NativeKeyEvent keyEvent = mock(NativeKeyEvent.class);
    when(keyEvent.getKeyCode()).thenReturn(NativeKeyEvent.VC_F5);

    new AppConfig().buildItemKeyListener(screenPrinter, settings).nativeKeyReleased(keyEvent);

    verify(screenPrinter, times(1)).run();
  }

  @Test
  void givenItemKeybindUnsetWhenPressingF3ThenDoNotRunItemScreenPrinter() {
    SettingRepository settings = mock(SettingRepository.class);
    when(settings.get(eq(Setting.PRINTSCREEN_ITEM_KEYBIND), anyInt()))
        .thenAnswer(invocation -> invocation.getArgument(1));
    ScreenPrinter screenPrinter = mock(ScreenPrinter.class);
    NativeKeyEvent keyEvent = mock(NativeKeyEvent.class);
    when(keyEvent.getKeyCode()).thenReturn(NativeKeyEvent.VC_F3);

    new AppConfig().buildItemKeyListener(screenPrinter, settings).nativeKeyReleased(keyEvent);

    verify(screenPrinter, never()).run();
  }
}
