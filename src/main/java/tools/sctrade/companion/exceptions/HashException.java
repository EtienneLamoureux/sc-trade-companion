package tools.sctrade.companion.exceptions;

public class HashException extends RuntimeException {
  private static final long serialVersionUID = -8754250166087313832L;

  public HashException(Exception e) {
    super(e);
  }
}
