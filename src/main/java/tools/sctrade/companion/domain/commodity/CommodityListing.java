package tools.sctrade.companion.domain.commodity;

import java.util.Collection;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

public record CommodityListing(User user, Collection<CommodityListing> listings) {

}
