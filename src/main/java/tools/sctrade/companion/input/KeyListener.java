package tools.sctrade.companion.input;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.util.Collection;
import java.util.Locale;
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
    logger.trace(
        String.format(Locale.ROOT, "Key pressed: %s", NativeKeyEvent.getKeyText(e.getKeyCode())));

    if (e.getKeyCode() == NativeKeyEvent.VC_F3) {
      runnables.parallelStream().forEach(n -> n.run());
    }
  }
}
