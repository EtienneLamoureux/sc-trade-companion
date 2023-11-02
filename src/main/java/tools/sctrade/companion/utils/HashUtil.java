package tools.sctrade.companion.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.imageio.ImageIO;
import tools.sctrade.companion.exceptions.HashException;

public class HashUtil {
  private static final String SHA3_256 = "SHA3-256";

  private HashUtil() {}

  public static String hash(String string) {
    return hash(string.getBytes(StandardCharsets.UTF_8));
  }

  public static String hash(BufferedImage image) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "jpg", outputStream);
      byte[] bytes = outputStream.toByteArray();

      return hash(bytes);
    } catch (IOException e) {
      throw new HashException(e);
    }
  }

  public static String hash(byte[] bytes) {
    try {
      final MessageDigest digest = MessageDigest.getInstance(SHA3_256);
      final byte[] hashbytes = digest.digest(bytes);

      return bytesToHex(hashbytes);
    } catch (Exception e) {
      throw new HashException(e);
    }
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder(2 * bytes.length);

    for (byte element : bytes) {
      String hex = Integer.toHexString(0xff & element);

      if (hex.length() == 1) {
        hexString.append('0');
      }

      hexString.append(hex);
    }

    return hexString.toString();
  }
}
