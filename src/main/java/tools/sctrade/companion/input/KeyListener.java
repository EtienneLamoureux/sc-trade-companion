package tools.sctrade.companion.input;

import java.util.Collection;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyListener implements NativeKeyListener {
  private final Logger logger = LoggerFactory.getLogger(KeyListener.class);

  private Collection<Runnable> runnables;

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
