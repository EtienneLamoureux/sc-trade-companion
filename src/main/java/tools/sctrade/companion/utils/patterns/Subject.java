package tools.sctrade.companion.utils.patterns;

import java.util.Collection;

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
