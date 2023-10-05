package tools.sctrade.companion;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import javax.imageio.ImageIO;

public class KeyListener implements NativeKeyListener {
  @Override
  public void nativeKeyPressed(NativeKeyEvent e) {
    System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

    if (e.getKeyCode() == NativeKeyEvent.VC_F3) {
      try {
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage capture = new Robot().createScreenCapture(screenRect);

        File imageFile = new File(String.format(Locale.ROOT, "screenshots/%s.bmp",
            String.valueOf(new Random().nextInt())));
        ImageIO.write(capture, "bmp", imageFile);
      } catch (IOException | AWTException e1) {
        e1.printStackTrace();
      }
    }
  }
}
