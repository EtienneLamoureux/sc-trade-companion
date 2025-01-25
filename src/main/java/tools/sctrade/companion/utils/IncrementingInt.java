package tools.sctrade.companion.utils;

/**
 * A simple class that holds an integer and provides a method to get the integer and increment it.
 */
public class IncrementingInt {
  private int integer;

  /**
   * Constructor for the class, sets the integer to 0.
   */
  public IncrementingInt() {
    integer = 0;
  }

  /**
   * Returns the integer.
   *
   * @return The integer.
   */
  public int get() {
    return integer;
  }

  /**
   * Increments the integer by 1 and returns the new value.
   *
   * @return The new value of the integer.
   */
  public int getAndIncrement() {
    return integer++;
  }
}
