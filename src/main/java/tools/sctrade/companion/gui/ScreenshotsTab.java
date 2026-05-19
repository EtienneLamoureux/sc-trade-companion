package tools.sctrade.companion.gui;

import java.awt.image.BufferedImage;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.imgscalr.Scalr;
import tools.sctrade.companion.domain.screenshot.Screenshot;
import tools.sctrade.companion.domain.screenshot.ScreenshotRepository;
import tools.sctrade.companion.domain.screenshot.ScreenshotStatus;
import tools.sctrade.companion.domain.screenshot.ScreenshotType;

/**
 * The screenshots tab for the companion GUI. Displays screenshot cards in a 3-column grid layout,
 * with newest-first ordering.
 */
public class ScreenshotsTab extends BorderPane {
  private static final int GRID_COLUMNS = 3;
  private static final int MAX_IMAGE_SIZE = 200;
  private static final String MUTED_STYLE = "-fx-text-fill: -fx-text-base-color;";

  private final ScreenshotRepository repository;
  private final GridPane gridPane;

  /**
   * Creates a new instance of the screenshots tab with no repository. This constructor is provided
   * for backward compatibility and will display an empty grid until a repository is provided.
   */
  public ScreenshotsTab() {
    this(new ScreenshotRepository());
  }

  /**
   * Creates a new instance of the screenshots tab.
   *
   * @param repository The screenshot repository.
   */
  public ScreenshotsTab(ScreenshotRepository repository) {
    this.repository = repository;
    this.gridPane = new GridPane();
    setupGridLayout();
    refreshCards();
    setCenter(gridPane);
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

  private void refreshCards() {
    Platform.runLater(() -> {
      gridPane.getChildren().clear();
      var screenshots = repository.getSnapshot();

      for (int i = 0; i < screenshots.size(); i++) {
        Screenshot screenshot = screenshots.get(i);
        VBox card = createCard(screenshot);
        int row = i / GRID_COLUMNS;
        int col = i % GRID_COLUMNS;
        gridPane.add(card, col, row);
      }
    });
  }

  private VBox createCard(Screenshot screenshot) {
    VBox card = new VBox(5);
    card.getStyleClass().add("screenshot-card");
    card.setPrefWidth(200);

    // Header: Title and Description
    Label title = new Label(getScreenshotTypeLabel(screenshot.type()));
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

  private String getScreenshotTypeLabel(ScreenshotType type) {
    return switch (type) {
      case COMMODITY_KIOSK -> "Commodity kiosk";
      case ITEM_KIOSK -> "Item kiosk";
    };
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
    body.setStyle(MUTED_STYLE);

    String statusText = getStatusText(screenshot);
    String icon = getStatusIcon(screenshot.status());

    if (!icon.isEmpty()) {
      Label iconLabel = new Label(icon);
      iconLabel.getStyleClass().add("screenshot-card-status-icon");
      body.getChildren().add(iconLabel);
    }

    Label statusLabel = new Label(statusText);
    statusLabel.getStyleClass().add("screenshot-card-status-text");
    statusLabel.setWrapText(true);
    body.getChildren().add(statusLabel);

    return body;
  }

  private String getStatusText(Screenshot screenshot) {
    return switch (screenshot.status()) {
      case QUEUED -> "In queue";
      case PROCESSING -> "Processing...";
      case SUCCESS -> screenshot.location() != null ? screenshot.location()
          : (screenshot.content() != null ? screenshot.content() : "Success");
      case ERROR -> screenshot.error() != null ? screenshot.error() : "Error";
    };
  }

  private String getStatusIcon(ScreenshotStatus status) {
    return switch (status) {
      case QUEUED -> "⏱"; // access_time emoji
      case PROCESSING -> "🔄"; // sync emoji
      case SUCCESS -> "✓"; // check_circle emoji
      case ERROR -> "✕"; // error emoji
    };
  }
}
