package tools.sctrade.companion.input;

import java.io.File;
import java.time.Duration;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.gamelog.FilePathObserver;
import tools.sctrade.companion.domain.gamelog.FilePathSubject;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.utils.LocalizationUtil;

public class FileTailer extends FilePathObserver {
  private static final Duration DELAY = Duration.ofSeconds(1);

  private static final Logger logger = LoggerFactory.getLogger(FileTailer.class);

  private TailerListener listener;
  private NotificationService notificationService;
  private Tailer tailer;


  public FileTailer(FilePathSubject subject, TailerListener listener,
      NotificationService notificationService) {
    super(subject);

    this.notificationService = notificationService;
    this.listener = listener;

    subject.attach(this);
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
      logger.info("Started to tail {}", this.filePath);
      notificationService.info(LocalizationUtil.get("infoTailingGameLogs"));
    } catch (Exception e) {
      logger.error("Could not tail file '{}'", this.filePath, e);
      notificationService.error(LocalizationUtil.get("errorTailingGameLogs"));
    }
  }

}
