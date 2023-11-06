package tools.sctrade.companion.utils;

import java.time.Instant;

public class TimeUtil {
  private TimeUtil() {}

  public static Instant getNow() {
    return Instant.now();
  }

  public static String getNowAsString(TimeFormat timeFormat) {
    return timeFormat.format(getNow());
  }
}
