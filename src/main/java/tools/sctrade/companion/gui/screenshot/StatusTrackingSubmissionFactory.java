package tools.sctrade.companion.gui.screenshot;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.SubmissionFactory;
import tools.sctrade.companion.utils.HashUtil;

/**
 * Decorator that wraps a {@link SubmissionFactory} and tracks screenshot processing state.
 *
 * <p>
 * The wrapped factory performs the actual submission extraction while this decorator persists
 * screenshot lifecycle updates to {@link ScreenshotRepository}.
 *
 * @param <T> the type of submission produced by this factory
 */
public class StatusTrackingSubmissionFactory<T> implements SubmissionFactory<T> {

  private final SubmissionFactory<T> submissionFactory;
  private final ScreenshotRepository screenshotRepository;
  private final ScreenshotType screenshotType;
  private final ScreenshotFactory screenshotFactory;

  /**
   * Creates a new status-tracking submission factory.
   *
   * @param submissionFactory wrapped submission factory
   * @param screenshotRepository repository used to track screenshot processing status
   * @param screenshotType kiosk type to associate with tracked screenshots
   * @param screenshotFactory screenshot record factory
   */
  public StatusTrackingSubmissionFactory(SubmissionFactory<T> submissionFactory,
      ScreenshotRepository screenshotRepository, ScreenshotType screenshotType,
      ScreenshotFactory screenshotFactory) {
    this.submissionFactory = submissionFactory;
    this.screenshotRepository = screenshotRepository;
    this.screenshotType = screenshotType;
    this.screenshotFactory = screenshotFactory;
  }

  @Override
  public T build(BufferedImage screenCapture) {
    String id = HashUtil.hash(screenCapture);
    screenshotRepository
        .upsert(screenshotFactory.buildProcessing(id, screenCapture, screenshotType));

    try {
      T result = submissionFactory.build(screenCapture);
      screenshotRepository
          .upsert(screenshotFactory.buildSuccess(id, screenCapture, result, screenshotType));
      return result;
    } catch (RuntimeException e) {
      screenshotRepository
          .upsert(screenshotFactory.buildError(id, screenCapture, e, screenshotType));
      throw e;
    }
  }
}
