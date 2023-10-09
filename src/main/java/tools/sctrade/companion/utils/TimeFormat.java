package tools.sctrade.companion.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public enum TimeFormat {
  SCREENSHOT("yyyy-MM-dd_H-m-s-S");

  public final String pattern;

  TimeFormat(String pattern) {
    this.pattern = pattern;
  }

  public String format(Instant instant) {
    return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault()).format(instant);
  }
}
