package tools.sctrade.companion.exceptions;

public class ThreadingException extends RuntimeException {
  private static final long serialVersionUID = 3582010715975918182L;

  public ThreadingException(InterruptedException e) {
    super(e);
  }
}
