package tools.sctrade.companion.exceptions;

/**
 * Exception thrown when an error occurs during image processing.
 */
public class ImageProcessingException extends RuntimeException {
  private static final long serialVersionUID = 1336518831573922685L;

  /**
   * Creates a new instance of the ImageProcessingException class.
   *
   * @param e The exception that caused the error
   */
  public ImageProcessingException(Exception e) {
    super(e);
  }
}
