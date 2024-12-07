package tools.sctrade.companion.utils.patterns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ChainOfResponsability<T extends Object> {
  private final Logger logger = LoggerFactory.getLogger(ChainOfResponsability.class);

  private ChainOfResponsability<T> next;

  public void setNext(ChainOfResponsability<T> next) {
    this.next = next;
  }

  public void process(T value) {
    if (canHandle(value)) {
      handle(value);
    } else if (next != null) {
      next.process(value);
    } else {
      logger.warn("No chain link could handle value '{}'", value);
    }
  }

  protected abstract boolean canHandle(T value);

  protected abstract void handle(T value);
}
