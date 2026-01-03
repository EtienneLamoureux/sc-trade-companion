package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;

/**
 * Represents a word as read by the OCR. This is functionally different from a
 * {@link LocatedFragment}.
 */
public class LocatedWord extends LocatedText {
  private String text;

  /**
   * Creates a new LocatedWord.
   *
   * @param text The text of the word.
   * @param boundingBox The location of the word in the image.
   */
  public LocatedWord(String text, Rectangle boundingBox) {
    this.text = text.strip();
    this.boundingBox = boundingBox;
  }

  @Override
  public String getText() {
    return text;
  }

  /**
   * Returns whether this word is separated from another word, i.e. the two words are part of
   * different columns of text. Words are considered separated if the gap in the X axis between the
   * two words is significant. <br />
   * N.B. there is more leeway for entirely numerical words.
   *
   * @param word The other word.
   * @return Whether the two words are separated.
   */
  public boolean isSeparatedFrom(LocatedText word) {
    double maxCharacterWidth = Math.max(getCharacterWidth(), word.getCharacterWidth());
    double leeway = 1.6 * maxCharacterWidth;
    double distanceBetweenWords = Math.abs(boundingBox.getMaxX() - word.getBoundingBox().getMinX());

    if (this.isNumerical() && word.isNumerical()) {
      leeway *= 2;
    }

    return distanceBetweenWords > leeway;
  }
}
