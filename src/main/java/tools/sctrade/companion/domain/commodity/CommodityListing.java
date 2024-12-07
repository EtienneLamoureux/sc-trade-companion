package tools.sctrade.companion.domain.commodity;

import java.time.Instant;

public record CommodityListing(String location, TransactionType transactionType, String commodity,
    Double price, Integer inventory, InventoryLevel inventoryLevel, Integer maxBoxSize,
    String batchId, Instant timestamp) {
  public CommodityListing(String location, TransactionType transactionType, String commodity,
      double price, int inventory, InventoryLevel inventoryLevel, String batchId,
      Instant timestamp) {
    this(location, transactionType, commodity, price, inventory, inventoryLevel, null, batchId,
        timestamp);
  }

  public CommodityListing(String location, TransactionType transactionType, String commodity,
      Integer maxBoxSize, String batchId, Instant timestamp) {
    this(location, transactionType, commodity, null, null, null, maxBoxSize, batchId, timestamp);
  }

  public CommodityListing withLocation(String location) {
    return new CommodityListing(location, transactionType(), commodity(), price(), inventory(),
        inventoryLevel(), batchId(), timestamp());
  }
}
