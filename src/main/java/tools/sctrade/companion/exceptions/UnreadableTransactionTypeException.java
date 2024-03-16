package tools.sctrade.companion.exceptions;

import tools.sctrade.companion.utils.LocalizationUtil;

public class UnreadableTransactionTypeException extends RuntimeException {
  private static final long serialVersionUID = 82552490134110748L;

  public UnreadableTransactionTypeException() {
    super(LocalizationUtil.get("errorUnreadableTransactionType"));
  }
}
