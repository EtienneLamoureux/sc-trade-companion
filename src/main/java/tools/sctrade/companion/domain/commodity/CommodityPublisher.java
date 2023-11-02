package tools.sctrade.companion.domain.commodity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

public abstract class CommodityPublisher {
  private final Logger logger = LoggerFactory.getLogger(CommodityPublisher.class);

  @Async
  public void publishAsynchronously(CommoditySubmission submission) {
    try {
      publish(submission);
    } catch (Exception e) {
      logger.error("Error while publishing commodity submission", e);
    }
  }

  protected abstract void publish(CommoditySubmission submission);
}
