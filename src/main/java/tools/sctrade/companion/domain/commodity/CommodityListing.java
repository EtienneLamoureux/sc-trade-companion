package tools.sctrade.companion.domain.commodity;

import java.sql.Timestamp;

public record CommodityListing(String location, String transaction, String commodity, int quantity,
    double price, String batchId, Timestamp timestamp) {

}
