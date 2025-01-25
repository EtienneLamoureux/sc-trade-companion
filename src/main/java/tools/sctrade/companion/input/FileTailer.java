package tools.sctrade.companion.input;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.utils.LocalizationUtil;
import tools.sctrade.companion.utils.patterns.Observer;
import tools.sctrade.companion.utils.patterns.Subject;

/**
 * Adapter for the Apache Commons IO Tailer to tail a file and notify a listener of changes.
 * 
 * @see Tailer
 */
public class FileTailer extends Observer<Path> {
  private static final Duration DELAY = Duration.ofSeconds(1);

  private static final Logger logger = LoggerFactory.getLogger(FileTailer.class);

  private TailerListener listener;
  private NotificationService notificationService;
  private Tailer tailer;

  /**
   * Creates a new instance of the file tailer.
   *
   * @param subject Path of the file to tail.
   * @param listener Listener to notify of changes.
   * @param notificationService Notification service.
   */
  public FileTailer(Subject<Path> subject, TailerListener listener,
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
      File file = new File(this.state.toString());
      tailer =
          Tailer.builder().setFile(file).setTailerListener(listener).setDelayDuration(DELAY).get();
      logger.info("Started to tail {}", this.state);
      notificationService.info(LocalizationUtil.get("infoTailingGameLogs"));
    } catch (Exception e) {
      logger.error("Could not tail file '{}'", this.state, e);
      notificationService.error(LocalizationUtil.get("errorTailingGameLogs"));
    }
  }

}
