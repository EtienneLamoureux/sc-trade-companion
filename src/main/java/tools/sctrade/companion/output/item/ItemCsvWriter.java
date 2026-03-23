package tools.sctrade.companion.output.item;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.item.ItemListing;
import tools.sctrade.companion.domain.item.ItemSubmission;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.utils.AsynchronousProcessor;
import tools.sctrade.companion.utils.CsvUtil;
import tools.sctrade.companion.utils.LocalizationUtil;
import tools.sctrade.companion.utils.TimeFormat;
import tools.sctrade.companion.utils.TimeUtil;

/**
 * Asynchronous processor to write item listings to a CSV file.
 */
public class ItemCsvWriter extends AsynchronousProcessor<ItemSubmission> {
  private final Logger logger = LoggerFactory.getLogger(ItemCsvWriter.class);

  private Path folder;

  /**
   * Creates a new instance of the item CSV writer.
   *
   * @param settings The settings repository.
   * @param notificationService The notification service.
   */
  public ItemCsvWriter(SettingRepository settings, NotificationService notificationService) {
    super(notificationService);

    folder = settings.get(Setting.MY_DATA_PATH);
    logger.info("CSV output path: {}", folder);
  }

  /**
   * Processes an item submission by writing its listings to a CSV file.
   */
  @Override
  public void process(ItemSubmission submission) {
    var filePath = buildFilePath();

    try {
      logger.debug("Writing {} item listings to '{}'...", submission.getListings().size(),
          filePath);
      Collection<List<String>> lines = buildLines(submission);
      CsvUtil.write(filePath, lines);
      logger.info("Wrote {} item listings to '{}'", submission.getListings().size(), filePath);
      notificationService
          .info(String.format(Locale.ROOT, LocalizationUtil.get("infoItemListingsCsvOutput"),
              submission.getListings().size(), filePath));
    } catch (Exception e) {
      logger.error("There was an error writing to '{}'", filePath, e);
    }
  }

  private Path buildFilePath() {
    String fileName = TimeUtil.getNowAsString(TimeFormat.CSV_FILENAME) + "_item-listings.csv";

    return Paths.get(folder.toString(), fileName);
  }

  private Collection<List<String>> buildLines(ItemSubmission submission) {
    Collection<List<String>> lines = new ArrayList<>();

    for (var listing : submission.getListings()) {
      lines.add(buildLine(listing));
    }

    return lines;
  }

  private List<String> buildLine(ItemListing listing) {
    return Arrays.asList(listing.name(), String.valueOf(listing.price()), listing.location(),
        listing.shop());
  }
}
