package tools.sctrade.companion.domain.item;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.user.User;

public class ItemSubmission {
  private final Logger logger = LoggerFactory.getLogger(ItemSubmission.class);

  private User user;
  private Collection<ItemListing> listings;

  public ItemSubmission(User user, Collection<ItemListing> listings) {
    this.user = user;
    this.listings = listings;
  }

  public User getUser() {
    return user;
  }

  public Collection<ItemListing> getListings() {
    return listings;
  }

  public boolean isEmpty() {
    return listings.isEmpty();
  }

}
