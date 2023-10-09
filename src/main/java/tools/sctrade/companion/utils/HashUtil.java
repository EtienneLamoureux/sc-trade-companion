package tools.sctrade.companion.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import tools.sctrade.companion.exceptions.HashException;

public class HashUtil {
  private static final String SHA3_256 = "SHA3-256";

  private HashUtil() {}

  public static String getSha256(String string) {
    try {
      final MessageDigest digest = MessageDigest.getInstance(SHA3_256);
      final byte[] hashbytes = digest.digest(string.getBytes(StandardCharsets.UTF_8));

      return bytesToHex(hashbytes);
    } catch (Exception e) {
      throw new HashException(e);
    }
  }

  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);

    for (byte element : hash) {
      String hex = Integer.toHexString(0xff & element);

      if (hex.length() == 1) {
        hexString.append('0');
      }

      hexString.append(hex);
    }

    return hexString.toString();
  }
}
