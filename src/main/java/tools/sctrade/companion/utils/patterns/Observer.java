package tools.sctrade.companion.utils.patterns;

/**
 * Observer base class in the observer pattern. Implementations are notified when the state of their
 * {@link Subject} changes.
 * 
 * @param <T> the type of the subject's state
 */
public abstract class Observer<T> {
  private Subject<T> subject;
  protected T state;

  protected Observer(Subject<T> subject) {
    this.subject = subject;
  }

  protected void update() {
    this.state = subject.getState();
  }

}
