package tools.sctrade.companion.utils.patterns;

import java.nio.file.Path;
import java.util.Collection;

public abstract class FilePathSubject {
  Collection<FilePathObserver> observers;
  Path filePath;

  public void attach(FilePathObserver observer) {
    observers.add(observer);
    setState();
  }

  public void detach(FilePathObserver observer) {
    observers.remove(observer);
  }

  public Path getState() {
    return filePath;
  }

  protected abstract void setState();

  protected void notifyObservers() {
    observers.forEach(n -> n.update());
  }
}
