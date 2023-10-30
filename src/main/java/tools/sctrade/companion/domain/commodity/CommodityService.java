package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageProcessor;

public class CommodityService extends ImageProcessor {
  private final Logger logger = LoggerFactory.getLogger(CommodityService.class);

  private CommoditySubmissionFactory submissionFactory;
  private Collection<CommodityPublisher> outputAdapters;

  public CommodityService(CommoditySubmissionFactory submissionFactory,
      Collection<CommodityPublisher> outputAdapters) {
    this.submissionFactory = submissionFactory;
    this.outputAdapters = outputAdapters;
  }

  @Override
  public void process(BufferedImage screenCapture) {
    var submission = submissionFactory.build(screenCapture);
    outputAdapters.stream().forEach(n -> n.publishAsynchronously(submission)); // TODO
  }
}
