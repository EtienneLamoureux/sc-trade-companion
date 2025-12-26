package tools.sctrade.companion.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ResourceUtil {
  public static List<String> getTextLines(String path) {
    try (InputStream inputStream = ResourceUtil.class.getResourceAsStream(path);
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(streamReader)) {

      var lines = new ArrayList<String>();
      bufferedReader.lines().forEach(n -> lines.add(n));

      return lines;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Get an image from a resource path.
   *
   * @param resourcePath The path to the image resource.
   * @return The image.
   * @throws IOException If the image could not be read/found.
   */
  public static BufferedImage getBufferedImage(String resourcePath) throws IOException {
    return ImageIO.read(ImageUtil.class.getResourceAsStream(resourcePath));
  }
}
