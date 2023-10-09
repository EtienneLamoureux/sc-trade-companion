package tools.sctrade.companion.domain.commodity;

import java.util.Collection;
import tools.sctrade.companion.domain.user.User;

public record CommoditySubmission(User user, Collection<CommodityListing> listings) {
}
