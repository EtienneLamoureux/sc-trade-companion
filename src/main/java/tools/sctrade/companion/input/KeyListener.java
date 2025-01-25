package tools.sctrade.companion.input;

import java.util.Collection;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for JNativeHook NativeKeyListener to listen for key events and trigger {@link Runnable}
 * downstream processes.
 *
 * @see NativeKeyListener
 */
public class KeyListener implements NativeKeyListener {
  private final Logger logger = LoggerFactory.getLogger(KeyListener.class);

  private Collection<Runnable> runnables;

  /**
   * Creates a new instance of the key listener.
   *
   * @param runnables The collection of {@link Runnable} to trigger when a specific key is pressed.
   */
  public KeyListener(Collection<Runnable> runnables) {
    this.runnables = runnables;
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent e) {
    logger.trace("Key pressed: {}", NativeKeyEvent.getKeyText(e.getKeyCode()));

    if (e.getKeyCode() == NativeKeyEvent.VC_F3) {
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
