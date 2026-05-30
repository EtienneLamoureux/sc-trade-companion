package tools.sctrade.companion.exceptions;

/**
 * Thrown when the OCR resource cannot be acquired within the allowed timeout, typically because
 * another OCR call is already running or has hung.
 *
 * <p>
 * Treated as a recoverable error so consumers log a warning, notify the user, and continue
 * processing subsequent items instead of restarting.
 */
public class OcrUnavailableException extends RecoverableProcessingException {
  private static final long serialVersionUID = 1L;

  /**
   * Creates an OcrUnavailableException with the given message.
   *
   * @param message description of why OCR is unavailable
   */
  public OcrUnavailableException(String message) {
    super(message);
  }
}
