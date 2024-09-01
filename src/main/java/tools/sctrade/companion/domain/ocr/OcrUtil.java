package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.utils.StringUtil;

public class OcrUtil {
  private OcrUtil() {}

  public static Rectangle getRectangleClosestTo(OcrResult result, String text) {
    var fragments = result.getColumns().parallelStream()
        .flatMap(n -> n.getFragments().parallelStream()).toList();
    LocatedFragment shopInventoryFragment = OcrUtil.findFragmentClosestTo(fragments, text);

    return shopInventoryFragment.getBoundingBox();
  }

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
