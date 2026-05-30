package tools.sctrade.companion.exceptions;

/**
 * Base type for processing exceptions that should not restart screenshot consumers.
 */
public abstract class RecoverableProcessingException extends RuntimeException {
  private static final long serialVersionUID = 7026546959728471254L;

  /**
   * Creates a recoverable processing exception with a message.
   *
   * @param message error message
   */
  protected RecoverableProcessingException(String message) {
    super(message);
  }
}
