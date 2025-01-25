package tools.sctrade.companion.domain.gamelog;

import tools.sctrade.companion.utils.patterns.ChainOfResponsability;

/**
 * This class is a base class for all game log line processors. It implements the Chain of
 * Responsability pattern and uses a regular expression to determine if it can handle a given line.
 */
public abstract class GameLogLineProcessor extends ChainOfResponsability<String> {

  protected String regex;

  @Override
  protected boolean canHandle(String value) {
    return value.matches(regex);
  }
}
