package tools.sctrade.companion;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class KeyListener implements NativeKeyListener {
  @Override
  public void nativeKeyPressed(NativeKeyEvent e) {
    System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

    if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
      try {
        GlobalScreen.unregisterNativeHook();
      } catch (NativeHookException nativeHookException) {
        nativeHookException.printStackTrace();
      }
    }
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent e) {
    System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent e) {
    System.out.println("Key Typed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
  }
}
