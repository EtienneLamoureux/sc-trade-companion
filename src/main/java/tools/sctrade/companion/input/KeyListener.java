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
  private Setting keybindSetting;
  private int defaultKeybind;

  /**
   * Creates a new instance of the key listener.
   *
   * @param runnables The collection of {@link Runnable} to trigger when a specific key is pressed.
   * @param settingRepository The setting repository to read the keybind from.
   * @param keybindSetting The {@link Setting} enum value to look up the keybind.
   * @param defaultKeybind The default keybind value if the setting is not configured.
   */
  public KeyListener(Collection<Runnable> runnables, SettingRepository settingRepository,
      Setting keybindSetting, int defaultKeybind) {
    this.runnables = runnables;
    this.settingRepository = settingRepository;
    this.keybindSetting = keybindSetting;
    this.defaultKeybind = defaultKeybind;
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent e) {
    // Deliberately empty
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent e) {
    // Deliberately empty
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent e) {
    logger.trace("Key pressed: {}", NativeKeyEvent.getKeyText(e.getKeyCode()));
    int keybind = settingRepository.get(keybindSetting, defaultKeybind);

    if (e.getKeyCode() == keybind) {
      runnables.parallelStream().forEach(n -> n.run());
    }
  }
}
