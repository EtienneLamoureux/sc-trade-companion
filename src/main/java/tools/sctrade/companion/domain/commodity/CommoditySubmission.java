package tools.sctrade.companion.domain.commodity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.user.User;

public class CommoditySubmission {
  private final Logger logger = LoggerFactory.getLogger(CommoditySubmission.class);

  private User user;
  private Collection<CommodityListing> listings;

  CommoditySubmission(User user, Collection<CommodityListing> listings) {
    this.user = user;
    this.listings = new ArrayList<>(listings);
  }

  public User getUser() {
    return user;
  }

  public Collection<CommodityListing> getListings() {
    return listings;
  }

  synchronized void merge(CommoditySubmission submission) {
    logger.debug("Merging {} new listings unto this submission's {} listings",
        submission.getListings().size(), listings.size());
    CommodityListing newListing = submission.getListings().iterator().next();

    /*
     * Kiosk location can only be correctly acquired from the "Sell" tab, since on the "Buy" tab,
     * the inventory listed would be the ship.
     */
    if (newListing.transactionType().equals(TransactionType.BUYS)) {
      listings = listings.parallelStream().map(listing -> {
        if (listing.transactionType().equals(TransactionType.SELLS)) {
          return listing;
        }

        return listing.withLocation(newListing.location());
      }).collect(Collectors.toList());
    }

    listings.addAll(submission.getListings());
  }
}
