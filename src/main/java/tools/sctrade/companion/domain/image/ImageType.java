package tools.sctrade.companion.domain.image;

import tools.sctrade.companion.utils.TimeFormat;
import tools.sctrade.companion.utils.TimeUtil;

/**
 * This enum is used to define the different types of images that can be saved to disk.
 */
public enum ImageType {
  SCREENSHOT(""), BUTTONS("_buttons"), BUY_BUTTON("_buy_button"), SELL_BUTTON(
      "_sell_button"), PREPROCESSED("_preprocessed");

  private String suffix;

  private ImageType(String suffix) {
    this.suffix = suffix;
  }

  /**
   * Generates a file name for an image of this type.
   *
   * @return File name for an image of this type.
   */
  public String generateFileName() {
    return TimeUtil.getNowAsString(TimeFormat.IMAGE_FILENAME) + suffix + ".jpg";
  }
}
