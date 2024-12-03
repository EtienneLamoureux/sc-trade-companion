package tools.sctrade.companion.utils.patterns;

import java.nio.file.Path;

public abstract class FilePathObserver {
  private FilePathSubject subject;
  protected Path filePath;

  protected FilePathObserver(FilePathSubject subject) {
    this.subject = subject;
  }

  protected void update() {
    this.filePath = subject.getState();
  }

}
