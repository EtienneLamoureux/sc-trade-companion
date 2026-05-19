package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import tools.sctrade.companion.domain.screenshot.ScreenshotRepository;
import tools.sctrade.companion.domain.screenshot.ScreenshotType;
import tools.sctrade.companion.exceptions.NoListingsException;

/**
 * Best-effort implementation that delegates to multiple {@link CommoditySubmissionFactory}
 * instances and returns the result with the most listings.
 */
public class BestEffortCommoditySubmissionFactory extends CommoditySubmissionFactory {

  private Collection<CommoditySubmissionFactory> commoditySubmissionFactory;

  /**
   * Constructor for {@link BestEffortCommoditySubmissionFactory}.
   *
   * @param screenshotRepository repository used to track screenshot processing status
   * @param screenshotType kiosk type to associate with tracked screenshots
   * @param commoditySubmissionFactory List of differently configured factories.
   */
  public BestEffortCommoditySubmissionFactory(ScreenshotRepository screenshotRepository,
      ScreenshotType screenshotType,
      Collection<CommoditySubmissionFactory> commoditySubmissionFactory) {
    super(screenshotRepository, screenshotType, null, null, null, null, null);
    this.commoditySubmissionFactory = commoditySubmissionFactory;
  }

  @Override
  protected CommoditySubmission doBuild(BufferedImage screenCapture) {
    return commoditySubmissionFactory.stream().map(factory -> {
      try {
        return factory.build(screenCapture);
      } catch (Exception e) {
        return null;
      }
    }).filter(submission -> submission != null && !submission.getListings().isEmpty())
        .max((s1, s2) -> Integer.compare(s1.getListings().size(), s2.getListings().size()))
        .orElseThrow(() -> new NoListingsException());
  }
}
