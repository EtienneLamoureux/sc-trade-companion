package tools.sctrade.companion.domain.gamelog;

import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

public class GameLogListener extends TailerListenerAdapter implements TailerListener {
  @Override
  public void handle(String line) {}

}
