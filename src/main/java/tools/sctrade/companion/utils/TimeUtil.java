package tools.sctrade.companion.utils;

import java.time.Instant;

public class TimeUtil {
  private TimeUtil() {}

  public static String getNowAsString(TimeFormat timeFormat) {
    return timeFormat.format(Instant.now());
  }
}
