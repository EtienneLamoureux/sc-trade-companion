package tools.sctrade.companion.domain.image;

import java.time.Instant;
import tools.sctrade.companion.utils.TimeFormat;

public enum ImageType {
  SCREENSHOT(""), BUY_BUTTON("_buy-button"), SELL_BUTTON("_sell-button"), PREPROCESSED(
      "_preprocessed");

  private String suffix;

  private ImageType(String suffix) {
    this.suffix = suffix;
  }

  public String generateFileName() {
    return TimeFormat.IMAGE_FILENAME.format(Instant.now()) + suffix + ".jpg";
  }
}
