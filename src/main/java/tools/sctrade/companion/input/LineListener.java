package tools.sctrade.companion.input;

import org.apache.commons.io.input.TailerListenerAdapter;
import tools.sctrade.companion.utils.ChainOfResponsability;

public class LineListener extends TailerListenerAdapter {
  private ChainOfResponsability<String> lineProcessor;

  public LineListener(ChainOfResponsability<String> lineHandler) {
    this.lineProcessor = lineHandler;
  }

  @Override
  public void handle(String line) {
    lineProcessor.process(line);
  }
}
