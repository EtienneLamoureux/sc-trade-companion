package tools.sctrade.companion.exceptions;

public class ImageProcessingException extends RuntimeException {
  private static final long serialVersionUID = 1336518831573922685L;

  public ImageProcessingException(Exception e) {
    super(e);
  }
}
