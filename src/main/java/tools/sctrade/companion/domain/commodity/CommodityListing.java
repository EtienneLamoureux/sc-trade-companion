package tools.sctrade.companion.domain.commodity;

import java.time.Instant;
import java.util.List;

/**
 * Represents a commodity listing: who buys what, at what price, and how much, and in which box
 * size. Also includes some metadata.
 */
public record CommodityListing(String location, TransactionType transactionType, String commodity,
    Double price, Integer inventory, InventoryLevel inventoryLevel, List<Integer> boxSizesInScu,
    String batchId, Instant timestamp) {
  /**
   * Constructor for {@link CommodityListing}.
   *
   * @param location Location where the commodity is being transacted.
   * @param transactionType Type of transaction (buy/sell).
   * @param commodity Name of the commodity.
   * @param price Price of the commodity.
   * @param inventory Current SCU amount.
   * @param inventoryLevel Level of inventory, aka how saturated the inventory is.
   * @param batchId Batch ID.
   * @param timestamp Time when the listing was recorded.
   */
  public CommodityListing(String location, TransactionType transactionType, String commodity,
      double price, int inventory, InventoryLevel inventoryLevel, String batchId,
      Instant timestamp) {
    this(location, transactionType, commodity, price, inventory, inventoryLevel, List.of(), batchId,
        timestamp);
  }

  /**
   * Constructor for {@link CommodityListing}. Omits inventory related information.
   *
   * @param location Location where the commodity is being transacted.
   * @param transactionType Type of transaction (buy/sell).
   * @param commodity Name of the commodity.
   * @param boxSizesInScu List of box sizes, in SCU
   * @param batchId Batch ID.
   * @param timestamp Time when the listing was recorded.
   */
  public CommodityListing(String location, TransactionType transactionType, String commodity,
      List<Integer> boxSizesInScu, String batchId, Instant timestamp) {
    this(location, transactionType, commodity, null, null, null, boxSizesInScu, batchId, timestamp);
  }

  /**
   * Creates a new {@link CommodityListing} with the specified location.
   *
   * @param location New location.
   * @return New {@link CommodityListing} with the specified location.
   */
  public CommodityListing withLocation(String location) {
    return new CommodityListing(location, transactionType(), commodity(), price(), inventory(),
        inventoryLevel(), batchId(), timestamp());
  }
}
