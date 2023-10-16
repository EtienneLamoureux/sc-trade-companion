package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LocatedFragment {
  protected Map<Double, LocatedWord> wordsByX;
  protected Rectangle boundingBox;

  public LocatedFragment(LocatedWord word) {
    super();
    wordsByX = new TreeMap<>();
    boundingBox = null;

    add(word);
  }

  public Rectangle getBoundingBox() {
    return boundingBox;
  }

  public String getText() {
    return wordsByX.values().stream().map(n -> n.getText()).collect(Collectors.joining(" "));
  }

  public List<LocatedWord> getWordsInReadingOrder() {
    return wordsByX.values().stream().toList();
  }

  public void add(LocatedWord word) {
    wordsByX.put(word.getBoundingBox().getCenterX(), word);

    if (boundingBox != null) {
      boundingBox.add(word.getBoundingBox());
    } else
      boundingBox = new Rectangle(word.getBoundingBox());
  }

}
