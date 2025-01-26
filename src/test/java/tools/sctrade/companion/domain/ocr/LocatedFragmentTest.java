package tools.sctrade.companion.domain.ocr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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

  static Object[][] provideFragmentsInSameColumn() {
    var fragment1 =
        new LocatedFragment(new LocatedWord("Construction", new Rectangle(0, 0, 10, 1)));
    fragment1.add(new LocatedWord("materials", new Rectangle(11, 0, 10, 1)));

    return new Object[][] {
        {"Left-aligned", fragment1,
            new LocatedFragment(new LocatedWord("High inventory", new Rectangle(10, 1, 10, 1)))},
        {"Right-aligned", fragment1,
            new LocatedFragment(new LocatedWord("High inventory", new Rectangle(15, 1, 5, 1)))}};
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideFragmentsInSameColumn")
  void givenFragmentsInSameSolumnhenCheckingIfFragmentsAreInTheSameColumnThenReturnTrue(String name,
      LocatedFragment fragment1, LocatedFragment fragment2) {
    boolean result = fragment1.isInTheSameColumnAs(fragment2);

    assertTrue(result);
  }

  static Object[][] provideFragmentsInDifferentColumn() {
    var fragment1 =
        new LocatedFragment(new LocatedWord("Construction", new Rectangle(0, 0, 10, 1)));
    fragment1.add(new LocatedWord("materials", new Rectangle(11, 0, 10, 1)));

    return new Object[][] {
        {"Touching", fragment1,
            new LocatedFragment(new LocatedWord("High inventory", new Rectangle(21, 1, 10, 1)))},
        {"Separated", fragment1,
            new LocatedFragment(new LocatedWord("High inventory", new Rectangle(25, 1, 10, 1)))}};
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideFragmentsInDifferentColumn")
  void givenFragmentsInDifferentColumnThenCheckingIfFragmentsAreInTheSameColumnThenReturnFalse(
      String name, LocatedFragment fragment1, LocatedFragment fragment2) {
    boolean result = fragment1.isInTheSameColumnAs(fragment2);

    assertFalse(result);
  }
}
