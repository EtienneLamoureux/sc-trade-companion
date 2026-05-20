package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import tools.sctrade.companion.domain.SubmissionFactory;
import tools.sctrade.companion.exceptions.NoListingsException;

/**
 * Best-effort implementation that delegates to multiple {@link CommoditySubmissionFactory}
 * instances and returns the result with the most listings.
 */
public class BestEffortCommoditySubmissionFactory
    implements SubmissionFactory<CommoditySubmission> {

  private Collection<CommoditySubmissionFactory> commoditySubmissionFactory;

  /**
   * Constructor for {@link BestEffortCommoditySubmissionFactory}.
   *
   * @param commoditySubmissionFactory List of differently configured factories.
   */
  public BestEffortCommoditySubmissionFactory(
      Collection<CommoditySubmissionFactory> commoditySubmissionFactory) {
    this.commoditySubmissionFactory = commoditySubmissionFactory;
  }

  @Override
  public CommoditySubmission build(BufferedImage screenCapture) {
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
