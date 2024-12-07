package tools.sctrade.companion.domain.gamelog.lineprocessors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.utils.patterns.ChainOfResponsability;

public class FallbackLogLineProcessor extends ChainOfResponsability<String> {
  private final Logger logger = LoggerFactory.getLogger(FallbackLogLineProcessor.class);

  @Override
  protected boolean canHandle(String value) {
    return true;
  }

  @Override
  protected void handle(String value) {
    logger.trace("No chain link could handle '{}'", value);
  }

}
