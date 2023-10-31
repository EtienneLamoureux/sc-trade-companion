package tools.sctrade.companion.domain.commodity;

import java.util.Collection;
import tools.sctrade.companion.domain.user.User;

public class CommoditySubmission {
  private User user;
  private Collection<CommodityListing> listings;

  CommoditySubmission(User user, Collection<CommodityListing> listings) {
    this.user = user;
    this.listings = listings;
  }

  synchronized void merge(CommoditySubmission submission) {
    // TODO Auto-generated method stub

  }
}
