package tools.sctrade.companion.utils.patterns;

import java.util.Collection;

/**
 * Subject base class in the observer pattern. It is used to notify implementations of
 * {@link Observer} when the state changes.
 * 
 * @param <T> the type of the subject's state
 */
public abstract class Subject<T> {
  protected Collection<Observer<T>> observers;
  protected T state;

  public void attach(Observer<T> observer) {
    observers.add(observer);
    setState();
  }

  public void detach(Observer<T> observer) {
    observers.remove(observer);
  }

  public T getState() {
    return state;
  }

  protected abstract void setState();

  protected void notifyObservers() {
    observers.forEach(n -> n.update());
  }
}
