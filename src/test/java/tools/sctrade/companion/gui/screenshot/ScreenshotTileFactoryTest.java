package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import atlantafx.base.controls.Tile;
import java.awt.image.BufferedImage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.gui.JavaFxTestUtil;

class ScreenshotTileFactoryTest {

  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenScreenshotInput_whenBuildingTile_thenTileContainsExpectedStyleAndContent() {
    ScreenshotTileFactory factory = new ScreenshotTileFactory(new ScreenshotRepository());
    Screenshot screenshot = screenshot("id-1", "Area18", ScreenshotStatus.SUCCESS);

    Tile tile = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> factory.build(screenshot));

    assertNotNull(tile);
    assertTrue(tile.getStyleClass().contains("screenshot-tile"));
    assertEquals(ScreenshotType.COMMODITY_KIOSK.label(), tile.getTitle());
    assertTrue(tile.getGraphic() instanceof ImageView);
    assertTrue(tile.getDescription().contains("Success"));
    assertTrue(tile.getDescription().contains("[span class=\"screenshot-tile-status"));
    assertTrue(tile.getDescription().contains(ScreenshotStatus.SUCCESS.glyph()));
  }

  @Test
  void givenScreenshotError_whenBuildingTile_thenDescriptionIncludesErrorMessageAndMarkup() {
    ScreenshotTileFactory factory = new ScreenshotTileFactory(new ScreenshotRepository());
    Screenshot screenshot = new Screenshot("id-1", screenshotImage(), null, ScreenshotStatus.ERROR,
        "Image too small", null, ScreenshotType.COMMODITY_KIOSK);

    Tile tile = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> factory.build(screenshot));

    assertTrue(tile.getDescription().contains("Image too small"));
    assertTrue(tile.getDescription().contains("[span class=\"screenshot-tile-status"));
    assertTrue(tile.getDescription().contains(ScreenshotStatus.ERROR.glyph()));
  }

  @Test
  void givenScreenshotErrorWithBrackets_whenBuildingTile_thenDescriptionSanitizesMarkup() {
    ScreenshotTileFactory factory = new ScreenshotTileFactory(new ScreenshotRepository());
    Screenshot screenshot = new Screenshot("id-1", screenshotImage(), null, ScreenshotStatus.ERROR,
        "Bad [value]", null, ScreenshotType.COMMODITY_KIOSK);

    Tile tile = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> factory.build(screenshot));

    assertTrue(tile.getDescription().contains("Bad ［value］"));
  }

  @Test
  void givenSameScreenshotId_whenBuildingTwice_thenImageIsReusedFromCache() {
    ScreenshotTileFactory factory = new ScreenshotTileFactory(new ScreenshotRepository());
    Screenshot first = screenshot("id-1", "Area18", ScreenshotStatus.SUCCESS);
    Screenshot second = screenshot("id-1", "Area18", ScreenshotStatus.PROCESSING);

    Image firstImage =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> extractImage(factory.build(first)));
    Image secondImage =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> extractImage(factory.build(second)));

    assertSame(firstImage, secondImage);
  }

  @Test
  void givenCacheExceedsCapacity_whenRebuildingOldId_thenOldImageIsEvicted() {
    ScreenshotRepository repository = new ScreenshotRepository();
    ScreenshotTileFactory factory = new ScreenshotTileFactory(repository);
    int capacity = repository.getCapacity();

    Image original = JavaFxTestUtil.supplyOnFxThreadAndWait(
        () -> extractImage(factory.build(screenshot("id-0", "A", ScreenshotStatus.SUCCESS))));

    for (int i = 1; i <= capacity; i++) {
      int index = i;
      JavaFxTestUtil.supplyOnFxThreadAndWait(
          () -> factory.build(screenshot("id-" + index, "A", ScreenshotStatus.SUCCESS)));
    }

    Image rebuilt = JavaFxTestUtil.supplyOnFxThreadAndWait(
        () -> extractImage(factory.build(screenshot("id-0", "A", ScreenshotStatus.SUCCESS))));

    assertNotSame(original, rebuilt);
  }

  private static Screenshot screenshot(String id, String location, ScreenshotStatus status) {
    return new Screenshot(id, screenshotImage(), location, status, null, null,
        ScreenshotType.COMMODITY_KIOSK);
  }

  private static BufferedImage screenshotImage() {
    return new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
  }

  private static Image extractImage(Tile tile) {
    return ((ImageView) tile.getGraphic()).getImage();
  }
}
