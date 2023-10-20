package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LocatedColumn extends LocatedText {
  private Map<Double, LocatedFragment> fragmentsByY;

  public LocatedColumn(LocatedFragment fragment) {
    super();
    fragmentsByY = new TreeMap<>();
    boundingBox = null;

    add(fragment);
  }


  @Override
  public String getText() {
    return fragmentsByY.values().stream().map(n -> n.getText())
        .collect(Collectors.joining(System.lineSeparator()));
  }

  public boolean shouldContain(LocatedFragment fragment) {
    double maxCharacterWidth = Math.max(getCharacterWidth(), fragment.getCharacterWidth());
    double leeway = 2 * maxCharacterWidth;
    var firstCharactersAlign =
        Math.abs(boundingBox.getMinX() - fragment.getBoundingBox().getMinX()) < leeway;
    var lastCharactersAlign =
        Math.abs(boundingBox.getMaxX() - fragment.getBoundingBox().getMaxX()) < leeway;

    return firstCharactersAlign || lastCharactersAlign;
  }

  public void add(LocatedFragment fragment) {
    fragmentsByY.put(fragment.getBoundingBox().getCenterY(), fragment);

    if (boundingBox != null) {
      boundingBox.add(fragment.getBoundingBox());
    } else
      boundingBox = new Rectangle(fragment.getBoundingBox());
  }
}
