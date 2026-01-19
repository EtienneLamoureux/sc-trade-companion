package tools.sctrade.companion.exceptions;

/**
 * Exception thrown when an error occurs while converting an object to JSON.
 */
public class JsonConversionException extends RuntimeException {
  private static final long serialVersionUID = -1657383566621485846L;

  /**
   * Creates a new instance of the JsonConversionException class.
   *
   * @param e The exception that caused this exception
   */
  public JsonConversionException(Exception e) {
    super(e);
  }
}
