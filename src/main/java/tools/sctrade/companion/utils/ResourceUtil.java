package tools.sctrade.companion.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

  /**
   * Copy a bundled resource to a temp file so APIs that require filesystem-backed URIs can use it.
   *
   * @param resourcePath The classpath resource path.
   * @return The temp file path containing the resource contents.
   */
  public static Path copyResourceToTempFile(String resourcePath) {
    try (InputStream inputStream = ResourceUtil.class.getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IllegalArgumentException("Resource not found: " + resourcePath);
      }

      String fileName = Paths.get(resourcePath).getFileName().toString();
      String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
      Path tempFile = Files.createTempFile("sc-trade-companion-", suffix);
      Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
      tempFile.toFile().deleteOnExit();
      return tempFile;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
