package tools.sctrade.companion.domain.commodity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
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
}
