package tools.sctrade.companion.domain.ocr;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.utils.StringUtil;

/**
 * Utility class for Optical Character Recognition (OCR) operations.
 */
public class OcrUtil {
  private OcrUtil() {}

  /**
   * Finds the fragment, from the provided result, that is closest (Levenshtein string distance) to
   * the target string.
   *
   * @param result The OCR result to search
   * @param text The target string
   * @return The fragment that is closest to the target string
   * @throws NoCloseStringException If no fragment is close enough to the target string
   */
  public static LocatedFragment findFragmentClosestTo(OcrResult result, String text) {
    var fragments = result.getColumns().parallelStream()
        .flatMap(n -> n.getFragments().parallelStream()).toList();

    return OcrUtil.findFragmentClosestTo(fragments, text);
  }

  /**
   * Finds the fragment, from the provided collection, that is closest (Levenshtein string distance)
   * to the target string.
   *
   * @param fragments The collection of fragments to search
   * @param string The target string
   * @return The fragment that is closest to the target string
   * @throws NoCloseStringException If no fragment is close enough to the target string
   */
  public static LocatedFragment findFragmentClosestTo(Collection<LocatedFragment> fragments,
      String string) {
    Map<Integer, LocatedFragment> fragmentsByDistanceToTarget = new ConcurrentSkipListMap<>();
    fragments.parallelStream().forEach(n -> fragmentsByDistanceToTarget
        .put(StringUtil.calculateLevenshteinDistance(n.getText(), string), n));

    if (fragmentsByDistanceToTarget.keySet().iterator().next() > (string.length() / 2)) {
      throw new NoCloseStringException(string);
    }

    return fragmentsByDistanceToTarget.values().iterator().next();
  }
}
