package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import tools.sctrade.companion.utils.MathUtil;

/**
 * Represents a column of text as read by the OCR. A column is a group of text fragments that are
 * vertically aligned.
 */
public class LocatedColumn extends LocatedText {
  private Map<Double, LocatedFragment> fragmentsByY;

  /**
   * Creates a new, empty column.
   */
  public LocatedColumn() {
    super();

    fragmentsByY = new TreeMap<>();
    boundingBox = null;
  }

  /**
   * Creates a new column, seeded with a single fragment.
   *
   * @param fragment The fragment to add to the column.
   */
  public LocatedColumn(LocatedFragment fragment) {
    this();

    add(fragment);
  }

  @Override
  public String getText() {
    return fragmentsByY.values().stream().map(n -> n.getText())
        .collect(Collectors.joining(System.lineSeparator()));
  }

  /**
   * Determines if the column should contain the provided fragment.
   *
   * @param fragment The fragment to check.
   * @return True if the column should contain the fragment, false otherwise.
   */
  public boolean shouldContain(LocatedFragment fragment) {
    return isLeftOrRightAligned(fragment) || hasSignificantXOverlap(fragment);
  }

  /**
   * Adds a fragment to the column.
   *
   * @param fragment The fragment to add.
   */
  public void add(LocatedFragment fragment) {
    fragmentsByY.put(fragment.getBoundingBox().getCenterY(), fragment);

    if (boundingBox != null) {
      boundingBox.add(fragment.getBoundingBox());
    } else {
      boundingBox = new Rectangle(fragment.getBoundingBox());
    }
  }

  public List<LocatedFragment> getFragments() {
    return fragmentsByY.values().stream().toList();
  }

  /**
   * Splits the column into paragraphs, based on the gap between the fragments.
   *
   * @return A list of paragraphs.
   */
  public List<LocatedColumn> getParagraphs() {
    double paragraphGap = getParagraphGap();
    List<LocatedColumn> paragraphs = new ArrayList<>();
    LocatedFragment previousFragment = null;

    for (var fragment : fragmentsByY.values()) {
      if (previousFragment != null) {
        var currentParagraph = paragraphs.get(paragraphs.size() - 1);
        var yGap =
            fragment.getBoundingBox().getMinY() - previousFragment.getBoundingBox().getMaxY();

        if (yGap < paragraphGap) {
          currentParagraph.add(fragment);
        } else {
          paragraphs.add(new LocatedColumn(fragment));
        }
      } else {
        paragraphs.add(new LocatedColumn(fragment));
      }

      previousFragment = fragment;
    }

    return paragraphs;
  }

  /**
   * Determines if the column overlaps with another column on the Y axis. Used to determine if two
   * columns are part of the same horizontal block of text, such as a commodity listing.
   *
   * @param column The column to check.
   * @return True if the columns overlap, false otherwise.
   */
  public boolean hasYOverlapWith(LocatedColumn column) {
    return ((column.getBoundingBox().getMaxY() - getBoundingBox().getMinY()) > 0)
        && ((getBoundingBox().getMaxY() - column.getBoundingBox().getMinY()) > 0);
  }

  private boolean isLeftOrRightAligned(LocatedFragment fragment) {
    var leeway = 1 * fragment.getCharacterWidth();

    var isLeftAligned =
        Math.abs(getBoundingBox().getMinX() - fragment.getBoundingBox().getMinX()) <= leeway;
    var isRightAligned =
        Math.abs(getBoundingBox().getMaxX() - fragment.getBoundingBox().getMaxX()) <= leeway;

    return isLeftAligned || isRightAligned;
  }

  private boolean hasSignificantXOverlap(LocatedFragment fragment) {
    var fragmentIsAtLeastHalfInColumn =
        getBoundingBox().getMinX() < fragment.getBoundingBox().getCenterX()
            && fragment.getBoundingBox().getCenterX() < getBoundingBox().getMaxX();
    var columnIsAtLeastHalfInFragment =
        fragment.getBoundingBox().getMinX() < getBoundingBox().getCenterX()
            && getBoundingBox().getCenterX() < fragment.getBoundingBox().getMaxX();

    var overlap = Math.min(getBoundingBox().getMaxX(), fragment.getBoundingBox().getMaxX())
        - Math.max(getBoundingBox().getMinX(), fragment.getBoundingBox().getMinX());
    var columnCoversMostOfFragment = (0.8 <= (overlap / fragment.getBoundingBox().getWidth()));
    var fragmentCoversMostOfColumn = (0.8 <= (overlap / fragment.getBoundingBox().getWidth()));

    return (fragmentIsAtLeastHalfInColumn && columnIsAtLeastHalfInFragment)
        || columnCoversMostOfFragment || fragmentCoversMostOfColumn;
  }

  /**
   * Calculates the gap, on the Y axis, above which the space between 2 lines means a new paragraph
   * has started
   *
   * TODO Can be improved by assuming the `yGaps` is a binomial distribution and picking the first
   * local minimum.
   *
   * @return Paragraph gap
   */
  private double getParagraphGap() {
    LocatedFragment previousFragment = null;
    var yGaps = new ArrayList<Double>();

    for (var fragment : fragmentsByY.values()) {
      if (previousFragment != null) {
        var yGap =
            fragment.getBoundingBox().getMinY() - previousFragment.getBoundingBox().getMaxY();
        if (yGap > 0) {
          yGaps.add(yGap);
        }
      }

      previousFragment = fragment;
    }

    yGaps.removeAll(MathUtil.calculateOuliers(yGaps));

    return MathUtil.calculateMean(yGaps);
  }
}
