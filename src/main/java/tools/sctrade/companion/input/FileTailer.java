package tools.sctrade.companion.input;

import java.io.File;
import java.time.Duration;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.gamelog.FilePathObserver;
import tools.sctrade.companion.domain.gamelog.FilePathSubject;

public class FileTailer extends FilePathObserver {
  private static final Duration DELAY = Duration.ofSeconds(1);

  private final Logger logger = LoggerFactory.getLogger(FileTailer.class);

  private TailerListener listener;
  private Tailer tailer;

  protected FileTailer(FilePathSubject subject, TailerListener listener) {
    super(subject);
  }

  @Override
  protected void update() {
    super.update();

    if (tailer != null) {
      tailer.close();
    }

    try {
      File file = new File(this.filePath.toString());
      tailer =
          Tailer.builder().setFile(file).setTailerListener(listener).setDelayDuration(DELAY).get();
    } catch (Exception e) {
      logger.error("Could not tail file '{}'", this.filePath, e);
    }
  }

}
