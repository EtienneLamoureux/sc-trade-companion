package tools.sctrade.companion.output.commodity;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.commodity.CommoditySubmission;
import tools.sctrade.companion.utils.AsynchronousProcessor;
import tools.sctrade.companion.utils.CsvUtil;
import tools.sctrade.companion.utils.TimeFormat;
import tools.sctrade.companion.utils.TimeUtil;

public class CommodityCsvWriter extends AsynchronousProcessor<CommoditySubmission> {
  private final Logger logger = LoggerFactory.getLogger(CommodityCsvWriter.class);

  private Path folder;

  public CommodityCsvWriter() {
    folder = Paths.get(".", "my-data").normalize().toAbsolutePath();
    logger.info("CSV output path: {}", folder);
  }

  @Override
  public void process(CommoditySubmission submission) {
    String fileName = TimeUtil.getNowAsString(TimeFormat.CSV_FILENAME) + ".csv";
    var filePath = Paths.get(folder.toString(), fileName).normalize().toAbsolutePath();
    Collection<List<String>> lines = new ArrayList<>();
    logger.debug("Writing commodity listings to '{}'...", filePath);

    for (var listing : submission.getListings()) {
      List<String> line = Arrays.asList(listing.location(), listing.transactionType().toString(),
          listing.commodity(), String.valueOf(listing.price()), String.valueOf(listing.inventory()),
          listing.inventoryLevel().getLabel(),
          TimeUtil.getAsString(TimeFormat.CSV_COLUMN, listing.timestamp()));
      lines.add(line);
    }

    CsvUtil.write(filePath, lines);
    logger.debug("Wrote commodity listings to '{}'", filePath);
  }
}
