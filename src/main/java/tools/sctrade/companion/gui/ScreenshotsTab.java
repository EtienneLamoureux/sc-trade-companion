package tools.sctrade.companion.gui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.imgscalr.Scalr;
import tools.sctrade.companion.gui.screenshot.Screenshot;
import tools.sctrade.companion.gui.screenshot.ScreenshotRepository;

import tools.sctrade.companion.utils.patterns.Observer;

/**
 * The screenshots tab for the companion GUI. Displays screenshot cards in a 3-column grid layout,
 * with newest-first ordering.
 */
public class ScreenshotsTab extends BorderPane {
  private static final int GRID_COLUMNS = 3;
  private static final int MAX_IMAGE_SIZE = 200;
  private static final String EMPTY_STATE_TEXT = "Capture screenshots and see their status here";

  private final GridPane gridPane;
  private final Label emptyStateLabel;
  private final Observer<List<Screenshot>> repositoryObserver;
  private final Map<String, VBox> cardsById = new HashMap<>();
  private final Map<String, CardRenderState> renderedStateById = new HashMap<>();

  /**
   * Creates a new instance of the screenshots tab.
   *
   * @param repository The screenshot repository.
   */
  public ScreenshotsTab(ScreenshotRepository repository) {
    this.gridPane = new GridPane();
    this.emptyStateLabel = createEmptyStateLabel();
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
    Platform.runLater(() -> {
      var safeScreenshots = screenshots == null ? List.<Screenshot>of() : screenshots;
      refreshCardsIncrementally(safeScreenshots);
    });
  }

  private void refreshCardsIncrementally(List<Screenshot> screenshots) {
    if (screenshots.isEmpty()) {
      cardsById.clear();
      renderedStateById.clear();
      gridPane.getChildren().clear();
      setCenter(emptyStateLabel);
      return;
    }

    setCenter(gridPane);
    List<Node> orderedNodes = new ArrayList<>();
    HashSet<String> activeIds = new HashSet<>();

    for (Screenshot screenshot : screenshots) {
      String id = screenshot.id();
      CardRenderState newState = CardRenderState.from(screenshot,
          screenshot.type().label(), getStatusText(screenshot));
      CardRenderState currentState = renderedStateById.get(id);

      if (!cardsById.containsKey(id) || !newState.equals(currentState)) {
        cardsById.put(id, createCard(screenshot));
        renderedStateById.put(id, newState);
      }

      orderedNodes.add(cardsById.get(id));
      activeIds.add(id);
    }

    cardsById.keySet().removeIf(id -> !activeIds.contains(id));
    renderedStateById.keySet().removeIf(id -> !activeIds.contains(id));

    gridPane.getChildren().setAll(orderedNodes);

    for (int i = 0; i < orderedNodes.size(); i++) {
      Node node = orderedNodes.get(i);
      GridPane.setRowIndex(node, i / GRID_COLUMNS);
      GridPane.setColumnIndex(node, i % GRID_COLUMNS);
    }
  }

  private VBox createCard(Screenshot screenshot) {
    VBox card = new VBox(5);
    card.getStyleClass().add("screenshot-card");
    card.setPrefWidth(200);

    // Header: Title and Description
    Label title = new Label(screenshot.type().label());
    title.getStyleClass().add("screenshot-card-title");

    Label description = new Label(screenshot.location() == null ? "..." : screenshot.location());
    description.getStyleClass().add("screenshot-card-description");
    description.setWrapText(true);

    BorderPane header = new BorderPane();
    header.setCenter(title);
    header.setBottom(description);
    card.getChildren().add(header);

    // Subheader: Image
    if (screenshot.image() != null) {
      ImageView imageView = createImageView(screenshot.image());
      card.getChildren().add(imageView);
    }

    // Body: Status
    VBox statusBody = createStatusBody(screenshot);
    card.getChildren().add(statusBody);

    return card;
  }

  private ImageView createImageView(BufferedImage bufferedImage) {
    BufferedImage scaled = scaleImage(bufferedImage, MAX_IMAGE_SIZE);
    Image fxImage = SwingFXUtils.toFXImage(scaled, null);
    ImageView imageView = new ImageView(fxImage);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(MAX_IMAGE_SIZE);
    imageView.setFitHeight(MAX_IMAGE_SIZE);
    return imageView;
  }

  private BufferedImage scaleImage(BufferedImage image, int maxSize) {
    if (image.getWidth() <= maxSize && image.getHeight() <= maxSize) {
      return image;
    }
    return Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, maxSize, maxSize);
  }

  private VBox createStatusBody(Screenshot screenshot) {
    VBox body = new VBox(5);
    body.setAlignment(Pos.CENTER);
    body.getStyleClass().add("screenshot-card-status-body");
    body.getStyleClass().add(screenshot.status().styleClass());

    Label iconLabel = new Label();
    iconLabel.getStyleClass().add("screenshot-card-status-icon");
    iconLabel.getStyleClass().add(screenshot.status().iconClass());
    body.getChildren().add(iconLabel);

    Label statusLabel = new Label(getStatusText(screenshot));
    statusLabel.getStyleClass().add("screenshot-card-status-text");
    statusLabel.setWrapText(true);
    body.getChildren().add(statusLabel);

    return body;
  }

  private String getStatusText(Screenshot screenshot) {
    return switch (screenshot.status()) {
      case SUCCESS -> screenshot.location() != null ? screenshot.location()
          : (screenshot.content() != null ? screenshot.content()
              : screenshot.status().defaultText());
      case ERROR -> screenshot.error() != null ? screenshot.error()
          : screenshot.status().defaultText();
      default -> screenshot.status().defaultText();
    };
  }

  private Label createEmptyStateLabel() {
    Label label = new Label(EMPTY_STATE_TEXT);
    label.setWrapText(true);
    label.setAlignment(Pos.CENTER);
    label.getStyleClass().addAll("title2", "screenshot-empty-state");
    BorderPane.setAlignment(label, Pos.CENTER);
    return label;
  }

  private record CardRenderState(String id, String typeLabel, String description, String statusText,
      String iconClass, String statusStyleClass, boolean hasImage, int imageWidth,
      int imageHeight) {
    private static CardRenderState from(Screenshot screenshot, String typeLabel,
        String statusText) {
      int width = screenshot.image() != null ? screenshot.image().getWidth() : -1;
      int height = screenshot.image() != null ? screenshot.image().getHeight() : -1;
      String description = screenshot.location() == null ? "..." : screenshot.location();
      return new CardRenderState(screenshot.id(), typeLabel, description, statusText,
          screenshot.status().iconClass(), screenshot.status().styleClass(),
          screenshot.image() != null, width, height);
    }
  }
}
