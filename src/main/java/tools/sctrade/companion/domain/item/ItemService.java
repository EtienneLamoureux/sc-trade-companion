package tools.sctrade.companion.domain.item;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.utils.AsynchronousProcessor;
import tools.sctrade.companion.utils.Consumer;

public class ItemService extends Consumer<BufferedImage> {

  private ItemSubmissionFactory submissionFactory;
  private Collection<AsynchronousProcessor<ItemSubmission>> publishers;

  public ItemService(BlockingQueue<BufferedImage> queue, NotificationService notificationService,
      ItemSubmissionFactory submissionFactory,
      Collection<AsynchronousProcessor<ItemSubmission>> publishers) {
    super(queue, notificationService);

    this.submissionFactory = submissionFactory;
    this.publishers = publishers;
  }

  @Override
  protected void consume(BufferedImage screenCapture) throws Exception {
    ItemSubmission submission = submissionFactory.build(screenCapture);

    if (!submission.isEmpty()) {
      publishers.forEach(p -> p.processAsynchronously(submission));
    }
  }
}
