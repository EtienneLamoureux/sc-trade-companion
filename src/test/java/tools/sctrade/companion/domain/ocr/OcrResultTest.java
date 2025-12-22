package tools.sctrade.companion.domain.ocr;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import java.util.List;
import org.junit.jupiter.api.Test;

class OcrResultTest {
  @Test
  void givenLocatedWordsWhenCroppingThenOnlyReturnContainedWords() {
    List<LocatedWord> locatedWords =
        List.of(new LocatedWord("inside, on edge", new Rectangle(0, 1, 1, 1)),
            new LocatedWord("inside, on corner", new Rectangle(0, 0, 1, 1)),
            new LocatedWord("fully inside", new Rectangle(1, 1, 1, 1)),
            new LocatedWord("inside, edge to edge", new Rectangle(0, 0, 2, 2)),
            new LocatedWord("outside, on edge", new Rectangle(0, 2, 1, 1)),
            new LocatedWord("outside, across edge", new Rectangle(0, 1, 1, 2)),
            new LocatedWord("outside, on corner", new Rectangle(2, 2, 1, 1)),
            new LocatedWord("fully outside", new Rectangle(10, 10, 1, 1)));
    var ocrResult = new OcrResult(locatedWords);

    var croppedOcrResults = ocrResult.crop(new Rectangle(0, 0, 2, 2));

    String textByLines = croppedOcrResults.getTextByLines();
    for (var expected : List.of("inside, on corner", "inside, on edge", "inside, edge to edge",
        "fully inside")) {
      assertTrue(textByLines.contains(expected));
    }
  }
}
