package tools.sctrade.companion.domain.gamelog;

import java.util.regex.Pattern;
import tools.sctrade.companion.utils.ChainOfResponsability;

public abstract class GameLogLineProcessor extends ChainOfResponsability<String> {

  protected Pattern pattern;

  @Override
  protected boolean canHandle(String value) {
    return pattern.matcher(value).matches();
  }
}
