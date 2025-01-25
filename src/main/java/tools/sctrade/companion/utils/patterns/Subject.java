package tools.sctrade.companion.utils.patterns;

import java.util.Collection;

/**
 * Subject base class in the observer pattern. It is used to notify implementations of
 * {@link Observer} when the state changes.
 *
 * @param <T> the type of the subject's state
 * @see <a href="https://en.wikipedia.org/wiki/Observer_pattern">Observer pattern</a>
 */
public abstract class Subject<T> {
  protected Collection<Observer<T>> observers;
  protected T state;

  /**
   * Attach an observer to the subject. Will be notified when the state changes.
   *
   * @param observer the observer to attach
   */
  public void attach(Observer<T> observer) {
    observers.add(observer);
    setState();
  }

  /**
   * Detach an observer from the subject. Will no longer be notified when the state changes.
   *
   * @param observer the observer to detach
   */
  public void detach(Observer<T> observer) {
    observers.remove(observer);
  }

  public T getState() {
    return state;
  }

  /**
   * Set the state of the subject. Will notify all attached observers.
   */
  protected abstract void setState();

  /**
   * Notify all attached observers that the state has changed.
   */
  protected void notifyObservers() {
    observers.forEach(n -> n.update());
  }
}
