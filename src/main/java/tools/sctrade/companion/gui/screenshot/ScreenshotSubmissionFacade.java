package tools.sctrade.companion.gui.screenshot;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.utils.Processor;

/**
 * Facade that records queued screenshots before delegating asynchronous processing.
 */
public class ScreenshotSubmissionFacade implements Processor<BufferedImage> {

  private final Processor<BufferedImage> asyncProcessor;
  private final ScreenshotRepository screenshotRepository;
  private final ScreenshotFactory screenshotFactory;
  private final ScreenshotType screenshotType;

  /**
   * Creates a new screenshot submission facade.
   *
   * @param asyncProcessor asynchronous processor for screenshot handling
   * @param screenshotRepository repository used to track screenshot status
   * @param screenshotFactory factory used to build screenshot records
   * @param screenshotType type associated with submitted screenshots
   */
  public ScreenshotSubmissionFacade(Processor<BufferedImage> asyncProcessor,
      ScreenshotRepository screenshotRepository, ScreenshotFactory screenshotFactory,
      ScreenshotType screenshotType) {
    this.asyncProcessor = asyncProcessor;
    this.screenshotRepository = screenshotRepository;
    this.screenshotFactory = screenshotFactory;
    this.screenshotType = screenshotType;
  }

  @Override
  public void process(BufferedImage screenCapture) {
    String id = StatusTrackingSubmissionFactory.computeId(screenshotType, screenCapture);
    screenshotRepository.upsert(screenshotFactory.buildQueued(id, screenCapture, screenshotType));
    asyncProcessor.process(screenCapture);
  }
}
