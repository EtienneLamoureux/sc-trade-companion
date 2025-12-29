package tools.sctrade.companion.domain.ocr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Group of {@link LocatedWord} that form a line. May be across multiple columns.
 */
public class LocatedLine extends LocatedFragment {
  public LocatedLine(LocatedWord word) {
    super(word);
  }

  public List<LocatedFragment> getFragments() {
    if (wordsByX.size() <= 1) {
      return Arrays.asList(this);
    }

    var fragments = new ArrayList<LocatedFragment>();
    LocatedWord previousWord = null;

    for (var word : getWordsInReadingOrder()) {
      if (previousWord == null || previousWord.isSeparatedFrom(word)) {
        fragments.add(new LocatedFragment(word));
      } else {
        fragments.get(fragments.size() - 1).add(word);
      }

      previousWord = word;
    }

    return fragments;
  }

  public boolean shouldContain(LocatedWord word) {
    return boundingBox.getMaxY() > word.getBoundingBox().getCenterY()
        && word.getBoundingBox().getCenterY() > boundingBox.getMinY();
  }

  public boolean isSeparatedFrom(LocatedLine line) {
    return Math.abs(boundingBox.getCenterY() - line.getBoundingBox().getCenterY()) > (2.0
        * Math.min(boundingBox.height, line.getBoundingBox().height));
  }
}
