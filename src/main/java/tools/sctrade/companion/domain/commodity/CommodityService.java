package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import tools.sctrade.companion.utils.AsynchronousProcessor;

public class CommodityService extends AsynchronousProcessor<BufferedImage> {
  private final Logger logger = LoggerFactory.getLogger(CommodityService.class);

  private CommoditySubmissionFactory submissionFactory;
  private Collection<AsynchronousProcessor<CommoditySubmission>> publishers;

  private Semaphore pendingSubmissionMutex = new Semaphore(1, true);
  private boolean publishNextTime;
  private Optional<CommoditySubmission> pendingSubmission;

  public CommodityService(CommoditySubmissionFactory submissionFactory,
      Collection<AsynchronousProcessor<CommoditySubmission>> publishers) {
    this.submissionFactory = submissionFactory;
    this.publishers = publishers;
    this.publishNextTime = false;
    this.pendingSubmission = Optional.empty();
  }

  @Override
  public void process(BufferedImage screenCapture) throws InterruptedException {
    CommoditySubmission submission = submissionFactory.build(screenCapture);

    try {
      pendingSubmissionMutex.acquire();

      if (pendingSubmission.isEmpty()) {
        pendingSubmission = Optional.of(submission);
      } else {
        pendingSubmission.get().merge(submission);
      }
    } finally {
      pendingSubmissionMutex.release();
    }
  }

  @Scheduled(fixedDelay = 60000)
  public void flush() throws InterruptedException {
    try {
      pendingSubmissionMutex.acquire();

      if (pendingSubmission.isEmpty()) {
        return;
      }

      if (!publishNextTime) {
        publishNextTime = true;
        return;
      }

      CommoditySubmission submission = pendingSubmission.get();
      publishers.stream().forEach(n -> n.processAsynchronously(submission));
      publishNextTime = false;
      pendingSubmission = Optional.empty();
    } finally {
      pendingSubmissionMutex.release();
    }
  }
}
