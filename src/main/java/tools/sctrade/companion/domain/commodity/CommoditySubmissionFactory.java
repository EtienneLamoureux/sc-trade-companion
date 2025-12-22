package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;

public interface CommoditySubmissionFactory {
  CommoditySubmission build(BufferedImage screenCapture);

  CommoditySubmission build(CommodityListing commodityListing);
}
