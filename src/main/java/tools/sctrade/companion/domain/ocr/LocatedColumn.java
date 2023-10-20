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
    double fraction = 0.25;
    
    for (var i = fraction; i < 1.0; i += fraction) {
      if (contains(
          fragment.getBoundingBox().getMinX() + (fragment.getBoundingBox().getWidth() * i))) {
        return true;
      }
    }

    return false;
  }


  public void add(LocatedFragment fragment) {
    fragmentsByY.put(fragment.getBoundingBox().getCenterY(), fragment);

    if (boundingBox != null) {
      boundingBox.add(fragment.getBoundingBox());
    } else
      boundingBox = new Rectangle(fragment.getBoundingBox());
  }


  private boolean contains(double x) {
    return boundingBox.getMinX() < x && x < boundingBox.getMaxX();
  }
}
