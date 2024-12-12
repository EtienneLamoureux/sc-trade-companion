package tools.sctrade.companion.utils;

public class IncrementingInt {
  private int integer;

  public IncrementingInt() {
    integer = 0;
  }

  public int get() {
    return integer;
  }

  public int getAndIncrement() {
    return integer++;
  }
}
