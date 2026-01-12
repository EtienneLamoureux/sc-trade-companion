package tools.sctrade.companion.exceptions;

/**
 * Exception thrown when an error occurs while parsing JSON.
 */
public class JsonParsingException extends RuntimeException {
  private static final long serialVersionUID = -1657383566621385846L;

  /**
   * Creates a new instance of the JsonParsingException class.
   *
   * @param e The exception that caused this exception
   */
  public JsonParsingException(Exception e) {
    super(e);
  }
}
