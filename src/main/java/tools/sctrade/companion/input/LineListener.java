package tools.sctrade.companion.input;

import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.utils.patterns.ChainOfResponsability;

/**
 * Adapter for the Apache Commons IO TailerListenerAdapter. This class listens for new lines and has
 * them processed by a {@link ChainOfResponsability}.
 *
 * @see TailerListener
 */
public class LineListener extends TailerListenerAdapter {
  private final Logger logger = LoggerFactory.getLogger(LineListener.class);

  private ChainOfResponsability<String> lineProcessor;

  /**
   * Creates a new instance of the line listener.
   *
   * @param lineProcessor The chain of responsability to process the lines.
   */
  public LineListener(ChainOfResponsability<String> lineProcessor) {
    this.lineProcessor = lineProcessor;
  }

  @Override
  public void handle(String line) {
    lineProcessor.process(line);
  }

  @Override
  public void handle(Exception ex) {
    logger.error("Error", ex);
  }

  @Override
  public void fileNotFound() {
    logger.debug("File not found");
  }

  @Override
  public void fileRotated() {
    logger.debug("File rotated");
  }
}
