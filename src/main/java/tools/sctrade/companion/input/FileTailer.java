package tools.sctrade.companion.input;

import tools.sctrade.companion.domain.gamelog.FilePathObserver;
import tools.sctrade.companion.domain.gamelog.FilePathSubject;

public class FileTailer extends FilePathObserver {

  protected FileTailer(FilePathSubject subject) {
    super(subject);
  }

  @Override
  protected void update() {
    super.update();
  }

}
