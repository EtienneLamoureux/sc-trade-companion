package tools.sctrade.companion.utils;

import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundUtil {
  private static final Logger logger = LoggerFactory.getLogger(SoundUtil.class);

  private SoundUtil() {}

  public static void play(String resourcePath) {
    try {
      InputStream inputStream = SoundUtil.class.getResourceAsStream(resourcePath);

      try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream)) {
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        clip.start();
      }
    } catch (Exception e) {
      logger.error("There was an error playing '{}'", resourcePath, e);
    }
  }
}
