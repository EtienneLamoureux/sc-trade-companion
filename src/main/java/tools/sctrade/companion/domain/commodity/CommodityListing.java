package tools.sctrade.companion.domain.commodity;

import java.time.Instant;

public record CommodityListing(String location, TransactionType transactionType, String commodity,
    double price, int inventory, InventoryLevel inventoryLevel, String batchId, Instant timestamp) {

  public CommodityListing withLocation(String location) {
    return new CommodityListing(location, transactionType(), commodity(), price(), inventory(),
        inventoryLevel(), batchId(), timestamp());
  }
}
