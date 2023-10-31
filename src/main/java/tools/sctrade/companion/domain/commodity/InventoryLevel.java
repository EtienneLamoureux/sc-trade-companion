package tools.sctrade.companion.domain.commodity;

enum InventoryLevel {
  NONE("no demand", 0), VERY_LOW("very low inventory", 1.0 / 6.0), LOW("low inventory",
      2.0 / 6.0), MEDIUM("medium inventory", 3.0 / 6.0), HIGH("high inventory",
          4.0 / 6.0), VERY_HIGH("very high inventory", 5.0 / 6.0), MAX("out of stock", 1.0);

  private final String string;
  private final double fraction;

  InventoryLevel(String string, double fraction) {
    this.string = string;
    this.fraction = fraction;
  }

  String getString() {
    return string;
  }

  int getInventory(int quantity, TransactionType transactionType) {
    if (TransactionType.SELLS.equals(transactionType)) {
      return quantity;
    }

    return (quantity == 0) ? 999999 : (int) (quantity / this.fraction) - quantity;
  }
}
