package tools.sctrade.companion.exceptions;

import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Thrown when a transaction type (buy/sell) could not be read from the image.
 */
public class UnreadableTransactionTypeException extends RuntimeException {
  private static final long serialVersionUID = 82552490134110748L;

  /**
   * Constructs a new UnreadableTransactionTypeException.
   */
  public UnreadableTransactionTypeException() {
    super(LocalizationUtil.get("errorUnreadableTransactionType"));
  }
}
