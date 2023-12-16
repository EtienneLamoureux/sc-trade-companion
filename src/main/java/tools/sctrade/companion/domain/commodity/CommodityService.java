package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.utils.AsynchronousProcessor;
import tools.sctrade.companion.utils.LocalizationUtil;

public class CommodityService extends AsynchronousProcessor<BufferedImage> {
  private final Logger logger = LoggerFactory.getLogger(CommodityService.class);

  private CommoditySubmissionFactory submissionFactory;
  private Collection<AsynchronousProcessor<CommoditySubmission>> publishers;

  private Semaphore mutex = new Semaphore(1, true);
  private boolean publishNextTime;
  private Optional<CommoditySubmission> pendingSubmission;

  public CommodityService(CommoditySubmissionFactory submissionFactory,
      Collection<AsynchronousProcessor<CommoditySubmission>> publishers,
      NotificationService notificationService) {
    super(notificationService);

    this.submissionFactory = submissionFactory;
    this.publishers = publishers;
    this.publishNextTime = false;
    this.pendingSubmission = Optional.empty();
  }

  @Override
  public void process(BufferedImage screenCapture) throws InterruptedException {
    CommoditySubmission submission = submissionFactory.build(screenCapture);
    notificationService.info(LocalizationUtil.get("infoCommodityListingsRead"));

    try {
      logger.debug("Acquiring mutex...");
      mutex.acquire();

      if (pendingSubmission.isEmpty()) {
        pendingSubmission = Optional.of(submission);
      } else {
        pendingSubmission.get().merge(submission);
      }
    } finally {
      mutex.release();
      logger.debug("Released mutex");
    }
  }

  @Scheduled(fixedDelay = 60000)
  public void flush() throws InterruptedException {
    try {
      logger.debug("Acquiring mutex...");
      mutex.acquire();

      if (pendingSubmission.isEmpty()) {
        logger.debug("Nothing to publish");
        return;
      }

      if (!publishNextTime) {
        publishNextTime = true;
        logger.info("Publish queued for next execution");
        return;
      }

      CommoditySubmission submission = pendingSubmission.get();

      if (submission.isLocated()) {
        logger.debug("Calling publishers...");
        publishers.stream().forEach(n -> n.processAsynchronously(submission));
        logger.debug("Called publishers");
      } else {
        logger.error("No commodity listings had a valid location");
        notificationService.error(LocalizationUtil.get("errorNoLocation"));
      }

      publishNextTime = false;
      pendingSubmission = Optional.empty();
    } finally {
      mutex.release();
      logger.debug("Released mutex");
    }
  }
}
