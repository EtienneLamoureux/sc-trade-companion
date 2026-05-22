package tools.sctrade.companion.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tools.sctrade.companion.gui.screenshot.Screenshot;
import tools.sctrade.companion.gui.screenshot.ScreenshotCardFactory;
import tools.sctrade.companion.gui.screenshot.ScreenshotRepository;
import tools.sctrade.companion.utils.patterns.Observer;

/**
 * The screenshots tab for the companion GUI. Displays screenshot cards in a responsive wrapping
 * layout, with newest-first ordering.
 */
public class ScreenshotsTab extends BorderPane {
  private static final String EMPTY_STATE_TEXT = "Capture screenshots and see their status here";

  private final FlowPane flowPane;
  private final ScrollPane scrollPane;
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
    this.flowPane = new FlowPane();
    this.scrollPane = new ScrollPane(flowPane);
    this.emptyStateLabel = createEmptyStateLabel();
    this.screenshotCardFactory = screenshotCardFactory;
    this.repositoryObserver = new Observer<>(repository) {
      @Override
      protected void update() {
        super.update();
        ScreenshotsTab.this.refreshCards(this.state);
      }
    };
    setupFlowLayout();
    setCenter(emptyStateLabel);
    repository.attach(repositoryObserver);
  }

  private void setupFlowLayout() {
    flowPane.setHgap(12);
    flowPane.setVgap(12);
    flowPane.setPadding(new Insets(16));
    flowPane.setAlignment(Pos.TOP_CENTER);
    flowPane.getStyleClass().add("screenshots-flow");
    flowPane.prefWrapLengthProperty().bind(Bindings.max(0, widthProperty().subtract(32)));

    scrollPane.getStyleClass().add("screenshots-scroll-pane");
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(false);
    scrollPane.setPannable(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
  }

  private void refreshCards(List<Screenshot> screenshots) {
    if (screenshots == null) {
      return;
    }

    if (Platform.isFxApplicationThread()) {
      refreshCardsIncrementally(screenshots);
      return;
    }

    Platform.runLater(() -> refreshCardsIncrementally(screenshots));
  }

  private void refreshCardsIncrementally(List<Screenshot> screenshots) {
    if (screenshots.isEmpty()) {
      cardsByHash.clear();
      flowPane.getChildren().clear();
      setCenter(emptyStateLabel);
      return;
    }

    setCenter(scrollPane);
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

    flowPane.getChildren().setAll(orderedNodes);
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
