package tools.sctrade.companion.exceptions;

public class CsvParsingException extends RuntimeException {
  private static final long serialVersionUID = 3678495141258249170L;

  public CsvParsingException(Exception e) {
    super(e);
  }
}
