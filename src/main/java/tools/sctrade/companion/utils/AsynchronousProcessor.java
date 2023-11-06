package tools.sctrade.companion.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

public abstract class AsynchronousProcessor<T> {
  private final Logger logger = LoggerFactory.getLogger(AsynchronousProcessor.class);

  @Async
  public void processAsynchronously(T unitOfWork) {
    try {
      process(unitOfWork);
    } catch (Exception e) {
      logger.error("Error while processing", e);
    }
  }

  protected abstract void process(T unitOfWork) throws Exception;
}
