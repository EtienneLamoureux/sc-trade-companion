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

  /**
   * Gets the midpoints of the gaps, on the X-axis, between this line's fragments.
   *
   * @return Ordered list, in reading order, of doubles
   */
  public List<Double> getXGapCenters() {
    var xGapCenters = new ArrayList<Double>();

    if (getFragments().isEmpty()) {
      return xGapCenters;
    }

    var fragmentsIterator = getFragments().iterator();
    var previousFragment = fragmentsIterator.next();

    while (fragmentsIterator.hasNext()) {
      var currentFragment = fragmentsIterator.next();
      double xGap =
          currentFragment.getBoundingBox().getMinX() - previousFragment.getBoundingBox().getMaxX();
      var xGapCenter = previousFragment.getBoundingBox().getMaxX() + (xGap / 2);
      xGapCenters.add(xGapCenter);
      previousFragment = currentFragment;
    }

    return xGapCenters;
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
