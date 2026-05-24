package tools.sctrade.companion.gui;

import atlantafx.base.controls.Tile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import tools.sctrade.companion.gui.screenshot.Screenshot;
import tools.sctrade.companion.gui.screenshot.ScreenshotRepository;
import tools.sctrade.companion.gui.screenshot.ScreenshotTileFactory;
import tools.sctrade.companion.utils.patterns.Observer;

/**
 * The screenshots tab for the companion GUI. Displays screenshot tiles in a single scrollable list,
 * with newest-first ordering.
 */
public class ScreenshotsTab extends javafx.scene.layout.BorderPane {
  private static final String EMPTY_STATE_TEXT = "Capture screenshots and see their status here";
  private static final double TILE_WIDTH_RATIO = 0.8d;

  private final VBox tileList;
  private final ScrollPane scrollPane;
  private final Label emptyStateLabel;
  private final Observer<List<Screenshot>> repositoryObserver;
  private final TileFactory<Screenshot> screenshotTileFactory;
  private final Map<Integer, Tile> tilesByHash = new HashMap<>();

  /**
   * Creates a new instance of the screenshots tab.
   *
   * @param repository The screenshot repository.
   */
  public ScreenshotsTab(ScreenshotRepository repository) {
    this(repository, new ScreenshotTileFactory(repository));
  }

  /**
   * Creates a new instance of the screenshots tab with an explicit tile factory dependency.
   *
   * @param repository The screenshot repository.
   * @param screenshotTileFactory tile factory dependency.
   */
  public ScreenshotsTab(ScreenshotRepository repository,
      TileFactory<Screenshot> screenshotTileFactory) {
    this.tileList = new VBox(12);
    this.scrollPane = new ScrollPane(tileList);
    this.emptyStateLabel = createEmptyStateLabel();
    this.screenshotTileFactory = screenshotTileFactory;
    this.repositoryObserver = new Observer<>(repository) {
      @Override
      protected void update() {
        super.update();
        ScreenshotsTab.this.refreshTiles(this.state);
      }
    };
    setupTileLayout();
    setCenter(emptyStateLabel);
    repository.attach(repositoryObserver);
  }

  private void setupTileLayout() {
    tileList.setPadding(new Insets(16));
    tileList.setAlignment(Pos.TOP_CENTER);
    tileList.setFillWidth(true);
    tileList.setSpacing(0);
    tileList.getStyleClass().add("screenshots-tile-list");

    scrollPane.getStyleClass().add("screenshots-scroll-pane");
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(false);
    scrollPane.setPannable(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
  }

  private void refreshTiles(List<Screenshot> screenshots) {
    if (screenshots == null) {
      return;
    }

    if (Platform.isFxApplicationThread()) {
      refreshTilesIncrementally(screenshots);
      return;
    }

    Platform.runLater(() -> refreshTilesIncrementally(screenshots));
  }

  private void refreshTilesIncrementally(List<Screenshot> screenshots) {
    if (screenshots.isEmpty()) {
      tilesByHash.clear();
      tileList.getChildren().clear();
      setCenter(emptyStateLabel);
      return;
    }

    setCenter(scrollPane);
    List<Node> orderedNodes = new java.util.ArrayList<>();
    HashSet<Integer> activeHashes = new HashSet<>();

    DoubleBinding tileWidth = Bindings.createDoubleBinding(
        () -> Math.max(0d, scrollPane.getViewportBounds().getWidth() * TILE_WIDTH_RATIO),
        scrollPane.viewportBoundsProperty());

    for (int i = 0; i < screenshots.size(); i++) {
      Screenshot screenshot = screenshots.get(i);
      Integer hash = screenshot.hashCode();
      if (!tilesByHash.containsKey(hash)) {
        Tile tile = screenshotTileFactory.build(screenshot);
        configureTileWidth(tile, tileWidth);
        tilesByHash.put(hash, tile);
      }

      orderedNodes.add(tilesByHash.get(hash));
      activeHashes.add(hash);
      if (i < screenshots.size() - 1) {
        orderedNodes.add(createSeparator(tileWidth));
      }
    }

    tilesByHash.keySet().removeIf(hash -> !activeHashes.contains(hash));
    tileList.getChildren().setAll(orderedNodes);
  }

  private void configureTileWidth(Tile tile, DoubleBinding tileWidth) {
    tile.setMinWidth(0d);
    tile.prefWidthProperty().bind(tileWidth);
    tile.maxWidthProperty().bind(tileWidth);
  }

  private Separator createSeparator(DoubleBinding tileWidth) {
    Separator separator = new Separator();
    separator.getStyleClass().add("screenshot-tile-separator");
    separator.setPadding(new Insets(8, 0, 8, 0));
    separator.setPrefWidth(0d);
    separator.prefWidthProperty().bind(tileWidth);
    separator.maxWidthProperty().bind(tileWidth);
    return separator;
  }

  private Label createEmptyStateLabel() {
    Label label = new Label(EMPTY_STATE_TEXT);
    label.setWrapText(true);
    label.setAlignment(Pos.CENTER);
    label.getStyleClass().addAll("title-2", "screenshot-empty-state");
    javafx.scene.layout.BorderPane.setAlignment(label, Pos.CENTER);
    return label;
  }
}
