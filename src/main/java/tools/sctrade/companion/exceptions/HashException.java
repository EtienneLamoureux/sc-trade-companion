package tools.sctrade.companion.exceptions;

/**
 * Exception thrown when an error occurs while hashing a file.
 */
public class HashException extends RuntimeException {
  private static final long serialVersionUID = -8754250166087313832L;

  /**
   * Creates a new instance of the HashException class.
   *
   * @param e The exception that caused this exception
   */
  public HashException(Exception e) {
    super(e);
  }
}
