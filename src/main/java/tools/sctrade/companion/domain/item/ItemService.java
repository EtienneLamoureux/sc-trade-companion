package tools.sctrade.companion.domain.item;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.utils.AsynchronousProcessor;

public class ItemService extends AsynchronousProcessor<BufferedImage> {

  protected ItemService(NotificationService notificationService) {
    super(notificationService);
  }

  @Override
  protected void process(BufferedImage screenCapture) throws Exception {

  }
}
