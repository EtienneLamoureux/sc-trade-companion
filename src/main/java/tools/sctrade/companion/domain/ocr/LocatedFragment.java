package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Group of {@link LocatedWord} that form a {@link LocatedLine}, or part of a line, i.e. one row of
 * one column of text
 */
public class LocatedFragment extends LocatedText {
  protected Map<Double, LocatedWord> wordsByX;

  /**
   * Creates a new fragment, seeded with a single word.
   *
   * @param word The word to add to the fragment.
   */
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

  /**
   * Add the word to the fragment.
   *
   * @param word The word to add.
   */
  public void add(LocatedWord word) {
    wordsByX.put(word.getBoundingBox().getCenterX(), word);

    if (boundingBox != null) {
      boundingBox.add(word.getBoundingBox());
    } else {
      boundingBox = new Rectangle(word.getBoundingBox());
    }
  }
}
