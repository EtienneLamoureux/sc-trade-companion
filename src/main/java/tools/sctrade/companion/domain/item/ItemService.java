package tools.sctrade.companion.domain.item;

import java.awt.image.BufferedImage;
import java.util.Collection;
import tools.sctrade.companion.domain.SubmissionFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.utils.AsynchronousProcessor;

public class ItemService extends AsynchronousProcessor<BufferedImage> {

  private SubmissionFactory<ItemSubmission> submissionFactory;
  private Collection<AsynchronousProcessor<ItemSubmission>> publishers;

  public ItemService(NotificationService notificationService,
      SubmissionFactory<ItemSubmission> submissionFactory,
      Collection<AsynchronousProcessor<ItemSubmission>> publishers) {
    super(notificationService);

    this.submissionFactory = submissionFactory;
    this.publishers = publishers;
  }

  @Override
  protected void doProcess(BufferedImage screenCapture) throws Exception {
    ItemSubmission submission = submissionFactory.build(screenCapture);

    if (!submission.isEmpty()) {
      publishers.forEach(p -> p.process(submission));
    }
  }

}
