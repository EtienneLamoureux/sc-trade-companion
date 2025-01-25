package tools.sctrade.companion.domain.commodity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.user.User;

/**
 * Represents a user's submission of commodity listings.
 */
public class CommoditySubmission {
  private final Logger logger = LoggerFactory.getLogger(CommoditySubmission.class);

  private User user;
  private Collection<CommodityListing> listings;

  CommoditySubmission(User user, Collection<CommodityListing> listings) {
    this.user = user;
    this.listings = new ArrayList<>(listings);
  }

  /**
   * Returns the user that submitted the listings.
   * 
   * @return the user that submitted the listings
   */
  public User getUser() {
    return user;
  }

  /**
   * Returns the listings submitted.
   * 
   * @return the listings submitted
   */
  public Collection<CommodityListing> getListings() {
    return listings;
  }

  /**
   * Merges the listings of another submission unto this submission. If the location is missing from
   * some of the listings, one is assigned from the located listings. This allows screenshots that
   * have been processed without selecting the location to still be valid if another screenshot
   * containing the location was at some point merged.
   * 
   * @param submission the submission to merge
   */
  synchronized void merge(CommoditySubmission submission) {
    logger.debug("Merging {} new listings unto this submission's {} listings",
        submission.getListings().size(), listings.size());
    listings.addAll(submission.getListings());
    Optional<String> location =
        listings.parallelStream().map(n -> n.location()).filter(n -> n != null).findAny();

    if (location.isPresent()) {
      var locatedListings =
          listings.parallelStream().filter(n -> n.location() != null).collect(Collectors.toList());
      locatedListings.addAll(listings.parallelStream().filter(n -> n.location() == null)
          .map(n -> n.withLocation(location.get())).collect(Collectors.toList()));
      listings = locatedListings;
    }
  }

  boolean isLocated() {
    return listings.parallelStream().anyMatch(n -> n.location() != null);
  }
}
