package tools.sctrade.companion.domain.commodity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.sctrade.companion.domain.ocr.LocatedColumn;

class RawCommodityListingTest {
  @ParameterizedTest(name = "{0}")
  @MethodSource("commodityListings")
  void givenValidCommodityListingsThenParseInformation(String name, LocatedColumn leftColumn,
      LocatedColumn rightColumn, String expectedComodity, InventoryLevel expectedInventoryLevel,
      Integer expectedInventory, double expectedPrice) {
    var listing = new RawCommodityListing(leftColumn, rightColumn);
  }
}
