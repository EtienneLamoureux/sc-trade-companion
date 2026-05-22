package tools.sctrade.companion.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import tools.sctrade.companion.gui.screenshot.Screenshot;
import tools.sctrade.companion.gui.screenshot.ScreenshotCardFactory;
import tools.sctrade.companion.gui.screenshot.ScreenshotRepository;
import tools.sctrade.companion.utils.patterns.Observer;

/**
 * The screenshots tab for the companion GUI. Displays screenshot cards in a 3-column grid layout,
 * with newest-first ordering.
 */
public class ScreenshotsTab extends BorderPane {
  private static final int GRID_COLUMNS = 3;
  private static final String EMPTY_STATE_TEXT = "Capture screenshots and see their status here";

  private final GridPane gridPane;
  private final Label emptyStateLabel;
  private final Observer<List<Screenshot>> repositoryObserver;
  private final CardFactory<Screenshot> screenshotCardFactory;
  private final Map<Integer, VBox> cardsByHash = new HashMap<>();

  /**
   * Creates a new instance of the screenshots tab.
   *
   * @param repository The screenshot repository.
   */
  public ScreenshotsTab(ScreenshotRepository repository) {
    this(repository, new ScreenshotCardFactory(repository));
  }

  /**
   * Creates a new instance of the screenshots tab with an explicit typed card factory dependency.
   *
   * @param repository The screenshot repository.
   * @param screenshotCardFactory typed screenshot card factory dependency.
   */
  public ScreenshotsTab(ScreenshotRepository repository,
      CardFactory<Screenshot> screenshotCardFactory) {
    this.gridPane = new GridPane();
    this.emptyStateLabel = createEmptyStateLabel();
    this.screenshotCardFactory = screenshotCardFactory;
    this.repositoryObserver = new Observer<>(repository) {
      @Override
      protected void update() {
        super.update();
        ScreenshotsTab.this.refreshCards(this.state);
      }
    };
    setupGridLayout();
    setCenter(emptyStateLabel);
    repository.attach(repositoryObserver);
  }

  private void setupGridLayout() {
    gridPane.setHgap(10);
    gridPane.setVgap(10);
    gridPane.getStyleClass().add("screenshots-grid");

    for (int i = 0; i < GRID_COLUMNS; i++) {
      ColumnConstraints col = new ColumnConstraints();
      col.setPercentWidth(100.0 / GRID_COLUMNS);
      col.setHgrow(Priority.ALWAYS);
      gridPane.getColumnConstraints().add(col);
    }
  }

  private void refreshCards(List<Screenshot> screenshots) {
    if (screenshots == null) {
      return;
    }

    Platform.runLater(() -> refreshCardsIncrementally(screenshots));
  }

  private void refreshCardsIncrementally(List<Screenshot> screenshots) {
    if (screenshots.isEmpty()) {
      cardsByHash.clear();
      gridPane.getChildren().clear();
      setCenter(emptyStateLabel);
      return;
    }

    setCenter(gridPane);
    List<Node> orderedNodes = new java.util.ArrayList<>();
    HashSet<Integer> activeHashes = new HashSet<>();

    for (Screenshot screenshot : screenshots) {
      Integer hash = screenshot.hashCode();
      if (!cardsByHash.containsKey(hash)) {
        cardsByHash.put(hash, screenshotCardFactory.build(screenshot));
      }

      orderedNodes.add(cardsByHash.get(hash));
      activeHashes.add(hash);
    }

    cardsByHash.keySet().removeIf(hash -> !activeHashes.contains(hash));

    gridPane.getChildren().setAll(orderedNodes);

    for (int i = 0; i < orderedNodes.size(); i++) {
      Node node = orderedNodes.get(i);
      GridPane.setRowIndex(node, i / GRID_COLUMNS);
      GridPane.setColumnIndex(node, i % GRID_COLUMNS);
    }
  }

  private Label createEmptyStateLabel() {
    Label label = new Label(EMPTY_STATE_TEXT);
    label.setWrapText(true);
    label.setAlignment(Pos.CENTER);
    label.getStyleClass().addAll("title-2", "screenshot-empty-state");
    BorderPane.setAlignment(label, Pos.CENTER);
    return label;
  }
}
