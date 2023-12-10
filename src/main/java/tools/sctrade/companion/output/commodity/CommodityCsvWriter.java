package tools.sctrade.companion.output.commodity;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.commodity.CommodityListing;
import tools.sctrade.companion.domain.commodity.CommoditySubmission;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.user.Setting;
import tools.sctrade.companion.domain.user.SettingRepository;
import tools.sctrade.companion.utils.AsynchronousProcessor;
import tools.sctrade.companion.utils.CsvUtil;
import tools.sctrade.companion.utils.LocalizationUtil;
import tools.sctrade.companion.utils.TimeFormat;
import tools.sctrade.companion.utils.TimeUtil;

public class CommodityCsvWriter extends AsynchronousProcessor<CommoditySubmission> {
  private final Logger logger = LoggerFactory.getLogger(CommodityCsvWriter.class);

  private Path folder;

  public CommodityCsvWriter(SettingRepository settings, NotificationService notificationService) {
    super(notificationService);

    folder = settings.get(Setting.MY_DATA_PATH);
    logger.info("CSV output path: {}", folder);
  }

  @Override
  public void process(CommoditySubmission submission) {
    var filePath = buildFilePath();

    try {
      logger.debug("Writing {} commodity listings to '{}'...", submission.getListings().size(),
          filePath);
      Collection<List<String>> lines = buildLines(submission);
      CsvUtil.write(filePath, lines);
      logger.info("Wrote {} commodity listings to '{}'", submission.getListings().size(), filePath);
      notificationService
          .notify(String.format(Locale.ROOT, LocalizationUtil.get("infoCommodityListingsCsvOutput"),
              submission.getListings().size(), filePath));
    } catch (Exception e) {
      logger.error("There was an error writing to '{}'", filePath, e);
    }
  }

  private Path buildFilePath() {
    String fileName = TimeUtil.getNowAsString(TimeFormat.CSV_FILENAME) + "_commodity-listings.csv";

    return Paths.get(folder.toString(), fileName);
  }

  private Collection<List<String>> buildLines(CommoditySubmission submission) {
    Collection<List<String>> lines = new ArrayList<>();

    for (var listing : submission.getListings()) {
      List<String> line = buildLine(listing);
      lines.add(line);
    }

    return lines;
  }

  private List<String> buildLine(CommodityListing listing) {
    return Arrays.asList(listing.location(), listing.transactionType().toString(),
        listing.commodity(), String.valueOf(listing.price()), String.valueOf(listing.inventory()),
        listing.inventoryLevel().getLabel(),
        TimeUtil.getAsString(TimeFormat.CSV_COLUMN, listing.timestamp()));
  }
}
