package tools.sctrade.companion.utils.patterns;

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
