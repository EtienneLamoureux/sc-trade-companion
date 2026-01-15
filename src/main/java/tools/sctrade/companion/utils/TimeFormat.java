package tools.sctrade.companion.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Enum for time formatting.
 */
public enum TimeFormat {
  IMAGE_FILENAME("yyyy-MM-dd_HH-mm-ss-n"), CSV_FILENAME("yyyy-MM"), CSV_COLUMN(
      "yyyy-MM-dd HH:mm:ss"), LOG_ENTRY("yyyy-MM-dd HH:mm:ss.SSS");

  public final String pattern;

  TimeFormat(String pattern) {
    this.pattern = pattern;
  }

  /**
   * Formats an instant.
   *
   * @param instant the instant to format
   * @return the formatted instant
   */
  public String format(Instant instant) {
    return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault()).format(instant);
  }
}
