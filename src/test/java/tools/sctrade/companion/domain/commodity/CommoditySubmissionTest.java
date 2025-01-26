package tools.sctrade.companion.domain.commodity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.user.User;

class CommoditySubmissionTest {
  private static final User USER = new User("id", "label");
  private static final String LOCATION = "tram & myers mining";
  private static final TransactionType TRANSACTION_TYPE = TransactionType.SELLS;
  private static final String COMMODITY = "waste";
  private static final double PRICE = 0;
  private static final int INVENTORY = 0;
  private static final InventoryLevel INVENTORY_LEVEL = InventoryLevel.MEDIUM;
  private static final String BATCH_ID = "batch id";
  private static final Instant TIMESTAMP = Instant.now();

  @BeforeEach
  void setUp() {}

  @Test
  void given_existing_submission_with_location_when_merging_submission_with_no_location_then_assign_location() {
    var submissionWithLocation =
        new CommoditySubmission(USER, Arrays.asList(new CommodityListing(LOCATION, TRANSACTION_TYPE,
            COMMODITY, PRICE, INVENTORY, INVENTORY_LEVEL, BATCH_ID, TIMESTAMP)));
    var submissionWithNoLocation =
        new CommoditySubmission(USER, Arrays.asList(new CommodityListing(null, TRANSACTION_TYPE,
            COMMODITY, PRICE, INVENTORY, INVENTORY_LEVEL, BATCH_ID, TIMESTAMP)));

    submissionWithLocation.merge(submissionWithNoLocation);

    assertEquals(0, submissionWithLocation.getListings().parallelStream()
        .filter(n -> n.location() == null).count());
  }

  @Test
  void given_existing_submission_with_no_location_when_merging_submission_with_location_then_assign_location() {
    var submissionWithNoLocation =
        new CommoditySubmission(USER, Arrays.asList(new CommodityListing(null, TRANSACTION_TYPE,
            COMMODITY, PRICE, INVENTORY, INVENTORY_LEVEL, BATCH_ID, TIMESTAMP)));
    var submissionWithLocation =
        new CommoditySubmission(USER, Arrays.asList(new CommodityListing(LOCATION, TRANSACTION_TYPE,
            COMMODITY, PRICE, INVENTORY, INVENTORY_LEVEL, BATCH_ID, TIMESTAMP)));

    submissionWithNoLocation.merge(submissionWithLocation);

    assertEquals(0, submissionWithNoLocation.getListings().parallelStream()
        .filter(n -> n.location() == null).count());
  }
}
