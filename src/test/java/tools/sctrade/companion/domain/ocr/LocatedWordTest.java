package tools.sctrade.companion.domain.ocr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LocatedWordTest {

  private static final String SOME_WORD = "Construction";
  private static final String SOME_OTHER_WORD = "Materials";

  static Object[][] provideWordsInSameColumn() {
    return new Object[][] {
        {"Same line, touching", new LocatedWord(SOME_WORD, new Rectangle(0, 0, 10, 1)),
            new LocatedWord(SOME_OTHER_WORD, new Rectangle(10, 0, 10, 1))},
        {"Same line, slightly separated", new LocatedWord(SOME_WORD, new Rectangle(0, 0, 10, 1)),
            new LocatedWord(SOME_OTHER_WORD, new Rectangle(11, 0, 10, 1))},
        {"Different lines, touching", new LocatedWord(SOME_WORD, new Rectangle(0, 0, 10, 1)),
            new LocatedWord(SOME_OTHER_WORD, new Rectangle(10, 1, 10, 1))},
        {"Different lines, slightly separated",
            new LocatedWord(SOME_WORD, new Rectangle(0, 0, 10, 1)),
            new LocatedWord(SOME_OTHER_WORD, new Rectangle(11, 1, 10, 1))}};
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideWordsInSameColumn")
  void givenWordsInSameColumnWhenCheckingIsSeparatedFromThenReturnFalse(String name,
      LocatedWord locatedWord1, LocatedWord locatedWord2) {
    var result = locatedWord1.isSeparatedFrom(locatedWord2);

    assertFalse(result);
  }

  static Object[][] provideWordsInDifferentColumns() {
    return new Object[][] {
        {"Same line, overlapping", new LocatedWord(SOME_WORD, new Rectangle(0, 0, 10, 1)),
            new LocatedWord(SOME_OTHER_WORD, new Rectangle(5, 0, 10, 1))},
        {"Different lines, overlapping", new LocatedWord(SOME_WORD, new Rectangle(0, 0, 10, 1)),
            new LocatedWord(SOME_OTHER_WORD, new Rectangle(5, 1, 10, 1))},
        {"Same line, very separated", new LocatedWord(SOME_WORD, new Rectangle(0, 0, 10, 1)),
            new LocatedWord(SOME_OTHER_WORD, new Rectangle(20, 0, 10, 1))},
        {"Different lines, very separated", new LocatedWord(SOME_WORD, new Rectangle(0, 0, 10, 1)),
            new LocatedWord(SOME_OTHER_WORD, new Rectangle(20, 1, 10, 1))}};
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideWordsInDifferentColumns")
  void givenWordsInDifferentColumnsWhenCheckingIsSeparatedFromThenReturnTrue(String name,
      LocatedWord locatedWord1, LocatedWord locatedWord2) {
    var result = locatedWord1.isSeparatedFrom(locatedWord2);

    assertTrue(result);
  }

  static Object[][] provideDifferentPotentiallyNumericalStrings() {
    return new Object[][] {{"Integer", "123", true}, {"Decimal", "3.14159", false},
        {"Alphanumerical", "a1b2c3", false}, {"Alphabetical", "bonjour", false},
        {"Integers", "1 2 4 16 24 32", true}};
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideDifferentPotentiallyNumericalStrings")
  void givenDifferentStringsWhenCheckingIsNumericalThenReturnExpected(String name, String string,
      boolean expected) {
    var locatedWord = new LocatedWord(string, new Rectangle());

    assertEquals(expected, locatedWord.isNumerical());
  }
}
