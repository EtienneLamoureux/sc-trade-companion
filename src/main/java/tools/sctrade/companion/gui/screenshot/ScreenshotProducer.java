package tools.sctrade.companion.gui.screenshot;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import tools.sctrade.companion.utils.HashUtil;
import tools.sctrade.companion.utils.patterns.Producer;

/**
 * A {@link Producer} that enqueues an image and, only on success, persists a {@link Screenshot}
 * record with status {@link ScreenshotStatus#QUEUED}.
 */
public class ScreenshotProducer extends Producer<BufferedImage> {

  private final ScreenshotRepository screenshotRepository;
  private final ScreenshotType screenshotType;

  /**
   * Creates a new instance.
   *
   * @param queue The queue to put images into.
   * @param screenshotRepository The repository used to persist screenshot records.
   * @param screenshotType The type of screenshot this producer handles.
   */
  public ScreenshotProducer(BlockingQueue<BufferedImage> queue,
      ScreenshotRepository screenshotRepository, ScreenshotType screenshotType) {
    super(queue);
    this.screenshotRepository = screenshotRepository;
    this.screenshotType = screenshotType;
  }

  @Override
  protected void produce(BufferedImage image) {
    super.produce(image);

    String screenshotId = HashUtil.hash(screenshotType.name() + ":" + HashUtil.hash(image));
    Screenshot screenshot = new Screenshot(screenshotId, image, "...", ScreenshotStatus.QUEUED,
        null, null, screenshotType);
    screenshotRepository.upsert(screenshot);
  }
}
