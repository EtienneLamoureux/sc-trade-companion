package tools.sctrade.companion.utils;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListMap;
import tools.sctrade.companion.exceptions.NoCloseStringException;

public class StringUtil {
  private StringUtil() {}

  public static int calculateLevenshteinDistance(String x, String y) {
    int[][] dp = new int[x.length() + 1][y.length() + 1];

    for (int i = 0; i <= x.length(); i++) {
      for (int j = 0; j <= y.length(); j++) {
        if (i == 0) {
          dp[i][j] = j;
        } else if (j == 0) {
          dp[i][j] = i;
        } else {
          dp[i][j] = Math.min(dp[i - 1][j - 1] + (x.charAt(i - 1) == y.charAt(j - 1) ? 0 : 1),
              Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
        }
      }
    }

    return dp[x.length()][y.length()];
  }

  public static String spellCheck(String string, Collection<String> possibilities) {
    var possibilitiesByDistance = new ConcurrentSkipListMap<Integer, String>();
    possibilities.parallelStream()
        .forEach(n -> possibilitiesByDistance.put(calculateLevenshteinDistance(n, string), n));

    var minDistance = possibilitiesByDistance.keySet().iterator().next();

    if (minDistance > (string.length() / 2)) {
      throw new NoCloseStringException(string);
    }

    return possibilitiesByDistance.get(minDistance);
  }

  public static String escapeBackslashes(String string) {
    return string.replace("\\", "\\\\");

  }
}
