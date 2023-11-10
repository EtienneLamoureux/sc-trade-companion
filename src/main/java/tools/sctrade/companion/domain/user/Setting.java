package tools.sctrade.companion.domain.user;

import java.nio.file.Paths;

public enum Setting {
  OUTPUT_SCREENSHOTS, OUTPUT_TRANSIENT_IMAGES, MY_IMAGES_PATH;

  @SuppressWarnings("unchecked")
  public <T> T cast(String value) {
    switch (this) {
      case OUTPUT_SCREENSHOTS, OUTPUT_TRANSIENT_IMAGES:
        return (T) Boolean.valueOf(value);
      case MY_IMAGES_PATH:
        return (T) Paths.get(value);
      default:
        return (T) value;
    }
  }
}
