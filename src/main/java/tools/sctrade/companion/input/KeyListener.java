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

    int configuredKeyCode = getConfiguredKeyCode();
    if (e.getKeyCode() == configuredKeyCode) {
      runnables.parallelStream().forEach(n -> n.run());
    }
  }

  private int getConfiguredKeyCode() {
    try {
      String keybindValue = settingRepository.get(Setting.PRINTSCREEN_KEYBIND, "F3");
      return parseKeyCode(keybindValue);
    } catch (Exception ex) {
      logger.warn("Could not read keybind setting, defaulting to F3", ex);
      return NativeKeyEvent.VC_F3;
    }
  }

  private int parseKeyCode(String keyName) {
    // Map key name to NativeKeyEvent key code
    switch (keyName.toUpperCase()) {
      case "F1":
        return NativeKeyEvent.VC_F1;
      case "F2":
        return NativeKeyEvent.VC_F2;
      case "F3":
        return NativeKeyEvent.VC_F3;
      case "F4":
        return NativeKeyEvent.VC_F4;
      case "F5":
        return NativeKeyEvent.VC_F5;
      case "F6":
        return NativeKeyEvent.VC_F6;
      case "F7":
        return NativeKeyEvent.VC_F7;
      case "F8":
        return NativeKeyEvent.VC_F8;
      case "F9":
        return NativeKeyEvent.VC_F9;
      case "F10":
        return NativeKeyEvent.VC_F10;
      case "F11":
        return NativeKeyEvent.VC_F11;
      case "F12":
        return NativeKeyEvent.VC_F12;
      default:
        logger.warn("Unknown key name: {}, defaulting to F3", keyName);
        return NativeKeyEvent.VC_F3;
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
