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

  User getUser() {
    return user;
  }

  Collection<CommodityListing> getListings() {
    return listings;
  }

  synchronized void merge(CommoditySubmission submission) {
    CommodityListing newListing = submission.getListings().iterator().next();

    /*
     * Kiosk location can only be correctly acquired from the "Sell" tab, since on the "Buy" tab,
     * the inventory listed would be the ship.
     */
    if (newListing.transaction().equals(TransactionType.BUYS.toString())) {
      listings = listings.parallelStream().map(listing -> {
        if (listing.transaction().equals(TransactionType.SELLS.toString())) {
          return listing;
        }

        return listing.withLocation(newListing.location());
      }).toList();
    }

    listings.addAll(submission.getListings());
  }
}
