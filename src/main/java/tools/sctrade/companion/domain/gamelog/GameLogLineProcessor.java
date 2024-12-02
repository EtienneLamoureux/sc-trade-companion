package tools.sctrade.companion.domain.gamelog;

import tools.sctrade.companion.utils.ChainOfResponsability;

public abstract class GameLogLineProcessor extends ChainOfResponsability<String> {

  protected String regex;

  @Override
  protected boolean canHandle(String value) {
    return value.matches(regex);
  }
}
