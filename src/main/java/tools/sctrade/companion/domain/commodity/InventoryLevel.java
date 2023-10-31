package tools.sctrade.companion.domain.commodity;

enum InventoryLevel {
  SOLD_OUT("out of stock", 0.0), VERY_LOW("very low inventory", 1.0 / 6.0), LOW("low inventory",
      2.0 / 6.0), MEDIUM("medium inventory", 3.0 / 6.0), HIGH("high inventory",
          4.0 / 6.0), VERY_HIGH("very high inventory",
              5.0 / 6.0), FULL("no demand", 1.0), MAX("max inventory", 1.0);

  private final String label;
  private final double saturation;

  InventoryLevel(String label, double saturation) {
    this.label = label;
    this.saturation = saturation;
  }

  String getLabel() {
    return label;
  }

  double getSaturation() {
    return saturation;
  }
}
