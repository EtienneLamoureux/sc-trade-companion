package tools.sctrade.companion.exceptions;

public class UserIdGenerationException extends RuntimeException {
  private static final long serialVersionUID = 9130198041763765708L;

  public UserIdGenerationException(Exception e) {
    super(e);
  }

  public UserIdGenerationException(String message) {
    super(message);
  }
}
