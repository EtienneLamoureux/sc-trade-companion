package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import atlantafx.base.controls.Tile;
import java.awt.image.BufferedImage;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.gui.screenshot.Screenshot;
import tools.sctrade.companion.gui.screenshot.ScreenshotRepository;
import tools.sctrade.companion.gui.screenshot.ScreenshotStatus;
import tools.sctrade.companion.gui.screenshot.ScreenshotTileFactory;
import tools.sctrade.companion.gui.screenshot.ScreenshotType;

class ScreenshotsTabTest {
  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenRepositoryWhenConstructedThenAcceptsRepository() {
    ScreenshotRepository repository = new ScreenshotRepository();
    ScreenshotsTab tab =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> new ScreenshotsTab(repository));
    assertNotNull(tab);
  }

  @Test
  void givenTileFactoryWhenConstructedThenAcceptsTileFactoryDependency() {
    ScreenshotRepository repository = new ScreenshotRepository();
    TileFactory<Screenshot> tileFactory = new ScreenshotTileFactory(repository);
    ScreenshotsTab tab =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> new ScreenshotsTab(repository, tileFactory));
    assertNotNull(tab);
  }

  @Test
  void givenEmptyRepositoryWhenRenderedThenDisplaysNoTiles() {
    ScreenshotRepository repository = new ScreenshotRepository();
    ScreenshotsTab tab =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> new ScreenshotsTab(repository));

    assertEquals(0, JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getTileCount(tab)));
  }

  @Test
  void givenRepositoryWithOneScreenshotWhenRenderedThenDisplaysOneTile() {
    ScreenshotRepository repository = new ScreenshotRepository();
    Screenshot screenshot = screenshot("id-1", "Commodity Exchange", ScreenshotStatus.SUCCESS);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    assertEquals(1, JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getTileCount(tab)));
  }

  @Test
  void givenRenderedTileWhenMeasuredThenUsesBoundWidthProperties() {
    ScreenshotRepository repository = new ScreenshotRepository();
    Screenshot screenshot = screenshot("id-1", "Commodity Exchange", ScreenshotStatus.SUCCESS);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    Tile tile = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getTileNode(tab, 0));
    assertTrue(tile.prefWidthProperty().isBound());
    assertTrue(tile.maxWidthProperty().isBound());
  }

  @Test
  void givenRepositoryWithMultipleScreenshotsWhenRenderedThenTilesAreInNewestFirstOrder() {
    ScreenshotRepository repository = new ScreenshotRepository();
    Screenshot old = screenshot("id-old", "Location A", ScreenshotStatus.SUCCESS);
    Screenshot middle =
        new Screenshot("id-middle", new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            "Location B", ScreenshotStatus.SUCCESS, null, null, ScreenshotType.ITEM_KIOSK);
    Screenshot newest = screenshot("id-newest", "Location C", ScreenshotStatus.SUCCESS);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(old);
      repository.upsert(middle);
      repository.upsert(newest);
      return new ScreenshotsTab(repository);
    });

    List<String> titles = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getTileTitles(tab));
    assertEquals(List.of("Commodity kiosk", "Item kiosk", "Commodity kiosk"), titles);
  }

  @Test
  void givenRepositoryWithMultipleScreenshotsWhenRenderedThenSeparatorsSplitTiles() {
    ScreenshotRepository repository = new ScreenshotRepository();
    repository.upsert(screenshot("id-1", "Location A", ScreenshotStatus.SUCCESS));
    repository.upsert(screenshot("id-2", "Location B", ScreenshotStatus.SUCCESS));

    ScreenshotsTab tab =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> new ScreenshotsTab(repository));

    assertEquals(1, JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getSeparatorCount(tab)));
  }

  @Test
  void givenTileWhenStatusIsSuccessThenShowsSuccessDescription() {
    ScreenshotRepository repository = new ScreenshotRepository();
    Screenshot screenshot = screenshot("id-1", "Commodity Exchange", ScreenshotStatus.SUCCESS);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    String description = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getTileDescription(tab, 0));
    assertTrue(description.contains("Read 1 listings"));
    assertTrue(description.contains("[span class=\"screenshot-tile-status"));
    assertTrue(description.contains(ScreenshotStatus.SUCCESS.glyph()));
  }

  @Test
  void givenTileWhenStatusIsErrorThenShowsErrorDescription() {
    ScreenshotRepository repository = new ScreenshotRepository();
    Screenshot screenshot =
        new Screenshot("id-1", new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB), null,
            ScreenshotStatus.ERROR, "Image too small", null, ScreenshotType.COMMODITY_KIOSK);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    String description = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getTileDescription(tab, 0));
    assertTrue(description.contains("Image too small"));
    assertTrue(description.contains("[span class=\"screenshot-tile-status"));
    assertTrue(description.contains(ScreenshotStatus.ERROR.glyph()));
  }

  @Test
  void givenTileWhenImageIsNullThenStillRendersTile() {
    ScreenshotRepository repository = new ScreenshotRepository();
    Screenshot screenshot = new Screenshot("id-1", null, "Test Location", ScreenshotStatus.SUCCESS,
        null, null, ScreenshotType.COMMODITY_KIOSK);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    assertEquals(1, JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getTileCount(tab)));
  }

  @Test
  void givenNonEmptyRepositoryWhenConstructedThenHasScrollableLayout() {
    ScreenshotRepository repository = new ScreenshotRepository();
    repository.upsert(screenshot("id-1", "Area18", ScreenshotStatus.SUCCESS));

    ScreenshotsTab tab =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> new ScreenshotsTab(repository));
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      // Wait for queued UI refresh.
    });

    assertTrue(JavaFxTestUtil.supplyOnFxThreadAndWait(() -> tab.getCenter() instanceof ScrollPane));
    assertTrue(JavaFxTestUtil.supplyOnFxThreadAndWait(
        () -> ((ScrollPane) tab.getCenter()).getContent() instanceof VBox));
  }

  @Test
  void givenOneUnchangedTileWhenRefreshingThenItsNodeIsReused() {
    ScreenshotRepository repository = new ScreenshotRepository();
    Screenshot unchanged = screenshot("id-unchanged", "Orison", ScreenshotStatus.SUCCESS);
    Screenshot changing =
        new Screenshot("id-changing", new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB), null,
            ScreenshotStatus.PROCESSING, null, null, ScreenshotType.ITEM_KIOSK);
    repository.upsert(unchanged);
    repository.upsert(changing);

    ScreenshotsTab tab =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> new ScreenshotsTab(repository));
    Tile unchangedTileBefore = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getTileNode(tab, 1));

    repository.upsert(
        new Screenshot("id-changing", new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB), null,
            ScreenshotStatus.ERROR, "OCR failed", null, ScreenshotType.ITEM_KIOSK));
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      // Wait for queued UI refresh.
    });

    Tile unchangedTileAfter = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getTileNode(tab, 1));
    assertSame(unchangedTileBefore, unchangedTileAfter);
    assertTrue(JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getTileDescription(tab, 0))
        .contains("OCR failed"));
  }

  @Test
  void givenNoScreenshotsWhenConstructedThenShowCenteredMutedTitle2EmptyStateMessage() {
    ScreenshotRepository repository = new ScreenshotRepository();
    ScreenshotsTab tab =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> new ScreenshotsTab(repository));

    assertEquals("Capture screenshots and see their status here",
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getEmptyStateText(tab)));
    assertTrue(JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getEmptyStateStyleClasses(tab))
        .contains("title-2"));
    assertTrue(JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getEmptyStateStyleClasses(tab))
        .contains("screenshot-empty-state"));
  }

  private Screenshot screenshot(String id, String location, ScreenshotStatus status) {
    String content = status == ScreenshotStatus.SUCCESS ? "Read 1 listings" : null;
    return new Screenshot(id, new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB), location,
        status, null, content, ScreenshotType.COMMODITY_KIOSK);
  }

  private int getTileCount(ScreenshotsTab tab) {
    VBox tileList = getTileList(tab);
    return tileList == null ? 0
        : (int) tileList.getChildren().stream().filter(Tile.class::isInstance).count();
  }

  private List<String> getTileTitles(ScreenshotsTab tab) {
    VBox tileList = getTileList(tab);
    if (tileList == null) {
      return java.util.Collections.emptyList();
    }

    return tileList.getChildren().stream().filter(Tile.class::isInstance).map(Tile.class::cast)
        .map(Tile::getTitle).toList();
  }

  private String getTileDescription(ScreenshotsTab tab, int tileIndex) {
    Tile tile = getTileNode(tab, tileIndex);
    return tile != null ? tile.getDescription() : "";
  }

  private Tile getTileNode(ScreenshotsTab tab, int tileIndex) {
    VBox tileList = getTileList(tab);
    List<Tile> tiles = getTiles(tileList);
    if (tiles == null || tileIndex >= tiles.size()) {
      return null;
    }

    return tiles.get(tileIndex);
  }

  private int getSeparatorCount(ScreenshotsTab tab) {
    VBox tileList = getTileList(tab);
    if (tileList == null) {
      return 0;
    }
    return (int) tileList.getChildren().stream().filter(Separator.class::isInstance).count();
  }

  private VBox getTileList(ScreenshotsTab tab) {
    if (tab.getCenter() instanceof ScrollPane scrollPane
        && scrollPane.getContent() instanceof VBox tileList) {
      return tileList;
    }
    return tab.getCenter() instanceof VBox tileList ? tileList : null;
  }

  private List<Tile> getTiles(VBox tileList) {
    if (tileList == null) {
      return null;
    }
    return tileList.getChildren().stream().filter(Tile.class::isInstance).map(Tile.class::cast)
        .toList();
  }

  private String getEmptyStateText(ScreenshotsTab tab) {
    return tab.getCenter() instanceof Label label ? label.getText() : "";
  }

  private List<String> getEmptyStateStyleClasses(ScreenshotsTab tab) {
    return tab.getCenter() instanceof Label label ? label.getStyleClass()
        : java.util.Collections.emptyList();
  }
}
