package tools.sctrade.companion.domain.commodity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.sctrade.companion.domain.ocr.LocatedColumn;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.LocatedWord;

class RawCommodityListingTest {

  static Object[][] provideValidBoxSizesInScuCases() {
    return new Object[][] {
        {"when the right text is a subset encoded without separators then parse the subset", "124",
            Optional.of(List.of(1, 2, 4))},
        {"when the right text uses internal whitespace then strip it before parsing", "1\t2\n4",
            Optional.of(List.of(1, 2, 4))},
        {"when the right text encodes the full set then parse every box size", "1248162432",
            Optional.of(List.of(1, 2, 4, 8, 16, 24, 32))}};
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideValidBoxSizesInScuCases")
  void givenBoxSizeTextWhenParsingThenReturnExpectedBoxSizesInScu(String name, String rightText,
      Optional<List<Integer>> expectedBoxSizesInScu) {
    var listing = new RawCommodityListing(columnWithText("foo"), columnWithText(rightText));

    assertEquals(expectedBoxSizesInScu, listing.getBoxSizesInScu());
  }

  static Object[][] provideInvalidBoxSizesInScuCases() {
    return new Object[][] {
        {"when the right text repeats an encoded size then return empty", "121", Optional.empty()},
        {"when the right text leaves leftover content then return empty", "1245", Optional.empty()},
        {"when the right text contains no encoded box sizes then return empty", "999",
            Optional.empty()}};
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideInvalidBoxSizesInScuCases")
  void givenInvalidBoxSizeTextWhenParsingThenReturnEmpty(String name, String rightText,
      Optional<List<Integer>> expectedBoxSizesInScu) {
    var listing = new RawCommodityListing(columnWithText("foo"), columnWithText(rightText));

    assertEquals(expectedBoxSizesInScu, listing.getBoxSizesInScu());
  }

  private static LocatedColumn columnWithText(String text) {
    return new LocatedColumn(new LocatedFragment(new LocatedWord(text, new Rectangle(0, 0, 1, 1))));
  }
}
