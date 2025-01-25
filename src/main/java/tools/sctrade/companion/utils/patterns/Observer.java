package tools.sctrade.companion.utils.patterns;

/**
 * Observer base class in the observer pattern. Implementations are notified when the state of their
 * {@link Subject} changes.
 *
 * @param <T> the type of the subject's state
 * @see <a href="https://en.wikipedia.org/wiki/Observer_pattern">Observer pattern</a>
 */
public abstract class Observer<T> {
  private Subject<T> subject;
  protected T state;

  /**
   * Constructor for the observer. It sets the subject for the observer.
   *
   * @param subject The subject to observe.
   */
  protected Observer(Subject<T> subject) {
    this.subject = subject;
  }

  /**
   * Retrieves the state of the subject and sets it in the observer.
   */
  protected void update() {
    this.state = subject.getState();
  }

}
