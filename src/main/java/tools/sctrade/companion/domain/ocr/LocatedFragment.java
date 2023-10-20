package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LocatedFragment extends LocatedText {
  protected Map<Double, LocatedWord> wordsByX;

  public LocatedFragment(LocatedWord word) {
    super();
    wordsByX = new TreeMap<>();
    boundingBox = null;

    add(word);
  }

  @Override
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

  public boolean isInTheSameColumnAs(LocatedFragment fragment) {
    double threshold = 2 * Math.max(getCharacterWidth(), fragment.getCharacterWidth());
    return (threshold < Math.abs(boundingBox.getMinX() - fragment.getBoundingBox().getMinX()))
        || (threshold < Math.abs(boundingBox.getMaxX() - fragment.getBoundingBox().getMaxX()));
  }
}
