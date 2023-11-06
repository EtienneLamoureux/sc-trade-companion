package tools.sctrade.companion.exceptions;

public class NoListingsException extends RuntimeException {
  private static final long serialVersionUID = -3711903740803747206L;

  public NoListingsException() {
    super("No listings were found in the image");
  }
}
