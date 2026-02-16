package tools.sctrade.companion.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;

/**
 * Tests for KeyListener.
 */
public class KeyListenerTest {
  private Runnable mockRunnable;
  private SettingRepository mockSettingRepository;
  private KeyListener keyListener;

  @BeforeEach
  public void setUp() {
    mockRunnable = mock(Runnable.class);
    mockSettingRepository = mock(SettingRepository.class);
  }

  @Test
  public void testDefaultKeybindF3() {
    // Arrange
    when(mockSettingRepository.get(eq(Setting.PRINTSCREEN_KEYBIND), eq("F3"))).thenReturn("F3");
    keyListener = new KeyListener(Arrays.asList(mockRunnable), mockSettingRepository);
    NativeKeyEvent event = mock(NativeKeyEvent.class);
    when(event.getKeyCode()).thenReturn(NativeKeyEvent.VC_F3);

    // Act
    keyListener.nativeKeyPressed(event);

    // Assert
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void testCustomKeybindF5() {
    // Arrange
    when(mockSettingRepository.get(eq(Setting.PRINTSCREEN_KEYBIND), eq("F3"))).thenReturn("F5");
    keyListener = new KeyListener(Arrays.asList(mockRunnable), mockSettingRepository);
    NativeKeyEvent event = mock(NativeKeyEvent.class);
    when(event.getKeyCode()).thenReturn(NativeKeyEvent.VC_F5);

    // Act
    keyListener.nativeKeyPressed(event);

    // Assert
    verify(mockRunnable, times(1)).run();
  }

  @Test
  public void testWrongKeyDoesNotTrigger() {
    // Arrange
    when(mockSettingRepository.get(eq(Setting.PRINTSCREEN_KEYBIND), eq("F3"))).thenReturn("F3");
    keyListener = new KeyListener(Arrays.asList(mockRunnable), mockSettingRepository);
    NativeKeyEvent event = mock(NativeKeyEvent.class);
    when(event.getKeyCode()).thenReturn(NativeKeyEvent.VC_F1);

    // Act
    keyListener.nativeKeyPressed(event);

    // Assert
    verify(mockRunnable, never()).run();
  }

  @Test
  public void testInvalidKeybindDefaultsToF3() {
    // Arrange
    when(mockSettingRepository.get(eq(Setting.PRINTSCREEN_KEYBIND), eq("F3")))
        .thenReturn("INVALID");
    keyListener = new KeyListener(Arrays.asList(mockRunnable), mockSettingRepository);
    NativeKeyEvent event = mock(NativeKeyEvent.class);
    when(event.getKeyCode()).thenReturn(NativeKeyEvent.VC_F3);

    // Act
    keyListener.nativeKeyPressed(event);

    // Assert
    verify(mockRunnable, times(1)).run();
  }
}
