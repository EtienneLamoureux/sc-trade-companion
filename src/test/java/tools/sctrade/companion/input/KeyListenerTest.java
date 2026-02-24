package tools.sctrade.companion.input;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;

@ExtendWith(MockitoExtension.class)
class KeyListenerTest {
  private static final int CONFIGURED_KEY = NativeKeyEvent.VC_F3;
  private static final int DIFFERENT_KEY = NativeKeyEvent.KEY_LOCATION_LEFT;

  @Mock
  private Runnable mockRunnable;
  @Mock
  private SettingRepository mockSettingRepository;

  private KeyListener keyListener;

  @Test
  void whenPressingConfiguredKeyThenCallRunnable() {
    when(mockSettingRepository.get(eq(Setting.PRINTSCREEN_KEYBIND), eq(CONFIGURED_KEY)))
        .thenReturn(CONFIGURED_KEY);
    NativeKeyEvent event = mock(NativeKeyEvent.class);
    when(event.getKeyCode()).thenReturn(CONFIGURED_KEY);
    keyListener = new KeyListener(Arrays.asList(mockRunnable), mockSettingRepository);

    keyListener.nativeKeyReleased(event);

    verify(mockRunnable, times(1)).run();
  }

  @Test
  void whenPressingWrongKeyThenDoesNotCallRunnable() {
    when(mockSettingRepository.get(eq(Setting.PRINTSCREEN_KEYBIND), eq(CONFIGURED_KEY)))
        .thenReturn(CONFIGURED_KEY);
    NativeKeyEvent event = mock(NativeKeyEvent.class);
    when(event.getKeyCode()).thenReturn(DIFFERENT_KEY);
    keyListener = new KeyListener(Arrays.asList(mockRunnable), mockSettingRepository);

    keyListener.nativeKeyReleased(event);

    verify(mockRunnable, never()).run();
  }
}
