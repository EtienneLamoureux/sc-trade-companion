package tools.sctrade.companion.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for playing sounds. Must be instanced to facilitate access to the resource files.
 */
public class SoundUtil {
  private static final Logger logger = LoggerFactory.getLogger(SoundUtil.class);

  /**
   * Creates a new instance of the sound util.
   */
  public SoundUtil() {}

  /**
   * Plays a sound from a resource path. Never throws an exception, but logs it if it occurs.
   *
   * @param resourcePath the path to the resource
   */
  public void play(String resourcePath) {
    try (InputStream inputStream =
        new BufferedInputStream(SoundUtil.class.getResourceAsStream(resourcePath))) {
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
