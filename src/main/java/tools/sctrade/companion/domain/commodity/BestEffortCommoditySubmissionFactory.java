package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import tools.sctrade.companion.exceptions.NoListingsException;

public class BestEffortCommoditySubmissionFactory extends CommoditySubmissionFactory {
  private Collection<CommoditySubmissionFactory> commoditySubmissionFactory;

  /**
   * Constructor for {@link BestEffortCommoditySubmissionFactory}.
   *
   * @param commodityListingFactory List of differently configured factories.
   */
  public BestEffortCommoditySubmissionFactory(
      Collection<CommoditySubmissionFactory> commoditySubmissionFactory) {
    super(null, null, null, null, null);
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
