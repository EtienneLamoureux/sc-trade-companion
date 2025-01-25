package tools.sctrade.companion.domain.gamelog.lineprocessors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.utils.patterns.ChainOfResponsability;

/**
 * This class is a fallback processor that will log a message when no other processor can handle the
 * input.
 */
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
