package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.commodity.CommodityListing;
import tools.sctrade.companion.domain.commodity.CommoditySubmission;
import tools.sctrade.companion.domain.commodity.TransactionType;
import tools.sctrade.companion.domain.item.ItemListing;
import tools.sctrade.companion.domain.item.ItemSubmission;

@ExtendWith(MockitoExtension.class)
class ScreenshotFactoryTest {

  @Mock
  private CommoditySubmission commoditySubmission;
  @Mock
  private ItemSubmission itemSubmission;

  private ScreenshotFactory screenshotFactory;
  private BufferedImage image;

  @BeforeEach
  void setUp() {
    screenshotFactory = new ScreenshotFactory();
    image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
  }

  @Test
  void givenAnyInput_whenBuildingProcessingScreenshot_thenStatusIsProcessing() {
    Screenshot screenshot =
        screenshotFactory.buildProcessing("id", image, ScreenshotType.COMMODITY_KIOSK);

    assertEquals(ScreenshotStatus.PROCESSING, screenshot.status());
  }

  @Test
  void givenCommoditySubmission_whenBuildingSuccessScreenshot_thenExtractsLocation() {
    when(commoditySubmission.getListings()).thenReturn(List.of(new CommodityListing("Area18",
        TransactionType.SELLS, "Aluminum", List.of(1), "batch", Instant.now())));

    Screenshot screenshot = screenshotFactory.buildSuccess("id", image, commoditySubmission,
        ScreenshotType.COMMODITY_KIOSK);

    assertEquals("Area18", screenshot.location());
  }

  @Test
  void givenItemSubmission_whenBuildingSuccessScreenshot_thenExtractsLocation() {
    when(itemSubmission.getListings())
        .thenReturn(List.of(new ItemListing("item", 1.0, "Orison", "shop")));

    Screenshot screenshot =
        screenshotFactory.buildSuccess("id", image, itemSubmission, ScreenshotType.ITEM_KIOSK);

    assertEquals("Orison", screenshot.location());
  }

  @Test
  void givenUnknownSubmissionType_whenBuildingSuccessScreenshot_thenLocationAndContentAreNull() {
    Screenshot screenshot =
        screenshotFactory.buildSuccess("id", image, "unknown", ScreenshotType.COMMODITY_KIOSK);

    assertNull(screenshot.location());
    assertNull(screenshot.content());
  }

  @Test
  void givenRuntimeException_whenBuildingErrorScreenshot_thenStatusAndErrorAreSet() {
    Screenshot screenshot = screenshotFactory.buildError("id", image, new RuntimeException("boom"),
        ScreenshotType.ITEM_KIOSK);

    assertEquals(ScreenshotStatus.ERROR, screenshot.status());
    assertEquals("boom", screenshot.error());
  }
}
