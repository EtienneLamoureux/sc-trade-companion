package tools.sctrade.companion.domain.ocr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.util.List;
import org.junit.jupiter.api.Test;

class LocatedFragmentTest {
  @Test
  void whenGettingWordsInReadingOrderThenReturnWordsInExpectedOrder() {
    LocatedWord word1 = new LocatedWord("Construction", new Rectangle(0, 0, 10, 1));
    LocatedWord word2 = new LocatedWord("materials", new Rectangle(11, 0, 20, 1));
    LocatedFragment fragment = new LocatedFragment(word2);
    fragment.add(word1);

    List<LocatedWord> words = fragment.getWordsInReadingOrder();

    assertEquals(word1, words.get(0));
    assertEquals(word2, words.get(1));
  }

  @Test
  void whenGettingTextThenGetStringInReadingOrder() {
    LocatedFragment fragment =
        new LocatedFragment(new LocatedWord("Construction", new Rectangle(0, 0, 10, 1)));
    fragment.add(new LocatedWord("materials", new Rectangle(11, 0, 20, 1)));

    String text = fragment.getText();

    assertEquals("Construction materials", text);
  }
}
