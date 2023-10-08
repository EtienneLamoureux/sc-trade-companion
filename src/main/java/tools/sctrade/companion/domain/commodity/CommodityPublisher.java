package tools.sctrade.companion.domain.commodity;

import org.springframework.scheduling.annotation.Async;

public interface CommodityPublisher {
  @Async
  void publishAsynchronously(CommoditySubmission submission);
}
