package tools.sctrade.companion.utils;

/**
 * Processor of units of work.
 *
 * @param <T> the type of the unit of work
 */
public interface Processor<T> {

  /**
   * Processes the unit of work.
   *
   * @param unitOfWork the unit of work to process
   */
  void process(T unitOfWork);
}
