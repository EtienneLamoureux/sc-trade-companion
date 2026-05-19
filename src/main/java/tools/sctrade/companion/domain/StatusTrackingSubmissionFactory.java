package tools.sctrade.companion.domain;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.screenshot.Screenshot;
import tools.sctrade.companion.domain.screenshot.ScreenshotRepository;
import tools.sctrade.companion.domain.screenshot.ScreenshotStatus;
import tools.sctrade.companion.domain.screenshot.ScreenshotType;
import tools.sctrade.companion.utils.HashUtil;

/**
 * Abstract base factory that implements screenshot status tracking around the build lifecycle.
 *
 * <p>
 * Subclasses implement {@link #doBuild(BufferedImage)} for the core factory logic and
 * {@link #extractLocation(Object)} / {@link #extractContent(Object)} to surface metadata from a
 * successful result. The template method {@link #build(BufferedImage)} wraps these with
 * {@link ScreenshotStatus#PROCESSING}, {@link ScreenshotStatus#SUCCESS}, or
 * {@link ScreenshotStatus#ERROR} upserts into the {@link ScreenshotRepository}.
 *
 * @param <T> the type of submission produced by this factory
 */
public abstract class StatusTrackingSubmissionFactory<T> implements SubmissionFactory<T> {

  private final ScreenshotRepository screenshotRepository;
  private final ScreenshotType screenshotType;

  /**
   * Creates a new status-tracking submission factory.
   *
   * @param screenshotRepository repository used to track screenshot processing status
   * @param screenshotType kiosk type to associate with tracked screenshots
   */
  protected StatusTrackingSubmissionFactory(ScreenshotRepository screenshotRepository,
      ScreenshotType screenshotType) {
    this.screenshotRepository = screenshotRepository;
    this.screenshotType = screenshotType;
  }

  @Override
  public final T build(BufferedImage screenCapture) {
    String id = HashUtil.hash(screenCapture);
    screenshotRepository.upsert(new Screenshot(id, screenCapture, null, ScreenshotStatus.PROCESSING,
        null, null, screenshotType));

    try {
      T result = doBuild(screenCapture);
      String location = extractLocation(result);
      String content = extractContent(result);
      screenshotRepository.upsert(new Screenshot(id, screenCapture, location,
          ScreenshotStatus.SUCCESS, null, content, screenshotType));
      return result;
    } catch (RuntimeException e) {
      screenshotRepository.upsert(new Screenshot(id, screenCapture, null, ScreenshotStatus.ERROR,
          e.getMessage(), null, screenshotType));
      throw e;
    }
  }

  /**
   * Core factory logic. Implementations should build and return the submission.
   *
   * @param screenCapture raw screenshot image
   * @return the built submission
   */
  protected abstract T doBuild(BufferedImage screenCapture);

  /**
   * Extracts the in-game location string from a successfully built result, or {@code null} if none
   * is available.
   *
   * @param result the successfully built submission
   * @return location string or {@code null}
   */
  protected abstract String extractLocation(T result);

  /**
   * Extracts a human-readable content summary from a successfully built result, or {@code null} if
   * none is applicable.
   *
   * @param result the successfully built submission
   * @return content summary or {@code null}
   */
  protected abstract String extractContent(T result);
}
