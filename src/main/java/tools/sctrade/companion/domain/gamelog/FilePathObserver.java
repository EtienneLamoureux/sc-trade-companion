package tools.sctrade.companion.domain.gamelog;

import java.nio.file.Path;

public abstract class FilePathObserver {
  private FilePathSubject subject;
  protected Path filePath;

  protected FilePathObserver(FilePathSubject subject) {
    this.subject = subject;
    subject.attach(this);
  }

  protected void update() {
    this.filePath = subject.getState();
  }

}
