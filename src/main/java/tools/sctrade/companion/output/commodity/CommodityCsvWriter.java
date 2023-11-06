package tools.sctrade.companion.output.commodity;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.commodity.CommoditySubmission;
import tools.sctrade.companion.utils.AsynchronousProcessor;

public class CommodityCsvWriter extends AsynchronousProcessor<CommoditySubmission> {
  private final Logger logger = LoggerFactory.getLogger(CommodityCsvWriter.class);

  private Path outputPath;

  public CommodityCsvWriter() {
    outputPath = Paths.get("./my-data").normalize().toAbsolutePath();
    logger.info("CSV output path: {}", outputPath);
  }

  @Override
  public void process(CommoditySubmission submission) {
    // TODO Auto-generated method stub

  }
}
