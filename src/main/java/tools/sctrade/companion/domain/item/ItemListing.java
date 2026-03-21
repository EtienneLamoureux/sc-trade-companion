package tools.sctrade.companion.domain.item;

public record ItemListing(String name, double price, String location, String shop) {
  public ItemListing withName(String name) {
    return new ItemListing(name, price, location, shop);
  }
}
