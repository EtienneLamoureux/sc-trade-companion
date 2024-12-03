package tools.sctrade.companion.domain.gamelog.lineprocessor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.utils.patterns.ChainOfResponsability;

public class OldLogLineProcessor extends ChainOfResponsability<String> {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT); // 2024-11-13T15:01:11.106Z

  private final Logger logger = LoggerFactory.getLogger(OldLogLineProcessor.class);

  private final Pattern pattern = Pattern.compile("^<(?<timestamp>.{24})> .+");
  private final Instant applicationOpenTime;

  public OldLogLineProcessor() {
    this.applicationOpenTime = Instant.now();
  }

  @Override
  protected boolean canHandle(String value) {
    var matcher = pattern.matcher(value);

    if (matcher.matches()) {
      var timestamp = matcher.group("timestamp");
      var timestampInstant =
          LocalDateTime.parse(timestamp, DATE_TIME_FORMATTER).atZone(ZoneId.of("UTC")).toInstant();

      return timestampInstant.isBefore(applicationOpenTime);
    } else {
      return false;
    }
  }

  @Override
  protected void handle(String value) {
    logger.trace("Old log line ignored: '{}'", value);
  }

}
