package tools.sctrade.companion.domain.commodity;

import java.sql.Timestamp;

public record CommodityListing(String location, String transaction, String commodity, double price,
    int inventory, double saturation, String batchId, Timestamp timestamp) {

}
