package tools.sctrade.companion.input;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.utils.patterns.ChainOfResponsability;

public class LineListener extends TailerListenerAdapter {
  private final Logger logger = LoggerFactory.getLogger(LineListener.class);

  private ChainOfResponsability<String> lineProcessor;

  public LineListener(ChainOfResponsability<String> lineProcessor) {
    this.lineProcessor = lineProcessor;
  }

  @Override
  public void handle(String line) {
    lineProcessor.process(line);
  }

  @Override
  public void fileNotFound() {
    logger.debug("File not found");
  }

  @Override
  public void fileRotated() {
    logger.debug("File rotated");
  }

  @Override
  public void handle(Exception ex) {
    logger.error("Error", ex);
  }
}
