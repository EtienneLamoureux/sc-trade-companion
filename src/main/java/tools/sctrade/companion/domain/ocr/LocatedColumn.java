package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import tools.sctrade.companion.utils.MathUtil;

public class LocatedColumn extends LocatedText {
  private Map<Double, LocatedFragment> fragmentsByY;

  public LocatedColumn(LocatedFragment fragment) {
    super();
    fragmentsByY = new TreeMap<>();
    boundingBox = null;

    add(fragment);
  }

  @Override
  public String getText() {
    return fragmentsByY.values().stream().map(n -> n.getText())
        .collect(Collectors.joining(System.lineSeparator()));
  }

  public boolean shouldContain(LocatedFragment fragment) {
    double fraction = 0.1;

    for (var i = fraction; i < 1.0; i += fraction) {
      if (contains(
          fragment.getBoundingBox().getMinX() + (fragment.getBoundingBox().getWidth() * i))) {
        return true;
      }
    }

    return false;
  }

  public void add(LocatedFragment fragment) {
    fragmentsByY.put(fragment.getBoundingBox().getCenterY(), fragment);

    if (boundingBox != null) {
      boundingBox.add(fragment.getBoundingBox());
    } else
      boundingBox = new Rectangle(fragment.getBoundingBox());
  }

  public List<LocatedFragment> getFragments() {
    return fragmentsByY.values().stream().toList();
  }

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

  public boolean hasYOverlapWith(LocatedColumn column) {
    return ((column.getBoundingBox().getMaxY() - getBoundingBox().getMinY()) > 0)
        && ((getBoundingBox().getMaxY() - column.getBoundingBox().getMinY()) > 0);
  }

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

  private boolean contains(double x) {
    return boundingBox.getMinX() < x && x < boundingBox.getMaxX();
  }
}
