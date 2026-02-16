package tools.sctrade.companion.input;

import java.util.Collection;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;

/**
 * Adapter for JNativeHook NativeKeyListener to listen for key events and trigger {@link Runnable}
 * downstream processes.
 *
 * @see NativeKeyListener
 */
public class KeyListener implements NativeKeyListener {
  private final Logger logger = LoggerFactory.getLogger(KeyListener.class);

  private Collection<Runnable> runnables;
  private SettingRepository settingRepository;

  /**
   * Creates a new instance of the key listener.
   *
   * @param runnables The collection of {@link Runnable} to trigger when a specific key is pressed.
   * @param settingRepository The setting repository to read the keybind from.
   */
  public KeyListener(Collection<Runnable> runnables, SettingRepository settingRepository) {
    this.runnables = runnables;
    this.settingRepository = settingRepository;
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent e) {
    logger.trace("Key pressed: {}", NativeKeyEvent.getKeyText(e.getKeyCode()));
    int keybind = settingRepository.get(Setting.PRINTSCREEN_KEYBIND, NativeKeyEvent.VC_F3);

    if (e.getKeyCode() == keybind) {
      runnables.parallelStream().forEach(n -> n.run());
    }
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeEvent) {
    // Deliberately empty
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
    // Deliberately empty
  }
}
