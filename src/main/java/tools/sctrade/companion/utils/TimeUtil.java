package tools.sctrade.companion.utils;

import java.time.Instant;

/**
 * Utility class for time manipulation.
 */
public class TimeUtil {
  private TimeUtil() {}

  public static Instant getNow() {
    return Instant.now();
  }

  /**
   * Returns the current time as a string in the specified format.
   *
   * @param timeFormat the format to use
   * @return the current time as a string
   */
  public static String getNowAsString(TimeFormat timeFormat) {
    return timeFormat.format(getNow());
  }

  /**
   * Returns the specified instant as a string in the specified format.
   *
   * @param timeFormat the format to use
   * @param instant the instant to format
   * @return the instant as a string
   */
  public static String getAsString(TimeFormat timeFormat, Instant instant) {
    return timeFormat.format(instant);
  }
}
