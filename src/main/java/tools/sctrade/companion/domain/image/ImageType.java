package tools.sctrade.companion.domain.image;

import tools.sctrade.companion.utils.TimeFormat;
import tools.sctrade.companion.utils.TimeUtil;

public enum ImageType {
  SCREENSHOT(""), BUTTONS("_buttons"), BUY_BUTTON("_buy_button"), SELL_BUTTON(
      "_sell_button"), PREPROCESSED("_preprocessed");

  private String suffix;

  private ImageType(String suffix) {
    this.suffix = suffix;
  }

  public String generateFileName() {
    return TimeUtil.getNowAsString(TimeFormat.IMAGE_FILENAME) + suffix + ".jpg";
  }
}
