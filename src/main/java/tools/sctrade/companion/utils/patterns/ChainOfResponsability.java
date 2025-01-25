package tools.sctrade.companion.utils.patterns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chain of responsability pattern implementation.
 * 
 * @param <T> Type of the value to be processed.
 */
public abstract class ChainOfResponsability<T extends Object> {
  private final Logger logger = LoggerFactory.getLogger(ChainOfResponsability.class);

  private ChainOfResponsability<T> next;

  /**
   * Set the next chain link.
   * 
   * @param next Next chain link.
   */
  public void setNext(ChainOfResponsability<T> next) {
    this.next = next;
  }

  /**
   * Process the value. If this chain link can handle the value, it will be processed, otherwise it
   * will be passed to the next chain link.
   * 
   * @param value Value to be processed.
   */
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
