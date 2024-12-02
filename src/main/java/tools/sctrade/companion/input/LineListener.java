package tools.sctrade.companion.input;

import org.apache.commons.io.input.TailerListenerAdapter;

public class LineListener extends TailerListenerAdapter {
  @Override
  public void handle(String line) {
    System.out.println(line);
  }
}
