package tools.sctrade.companion.exceptions;

/**
 * Exception thrown when an error occurs while parsing a CSV file.
 */
public class CsvParsingException extends RuntimeException {
  private static final long serialVersionUID = 3678495141258249170L;

  /**
   * Creates a new instance of the CsvParsingException class.
   * 
   * @param e The exception that caused this exception
   */
  public CsvParsingException(Exception e) {
    super(e);
  }
}
