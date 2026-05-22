package tools.sctrade.companion.gui.screenshot;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.imgscalr.Scalr;
import org.kordamp.ikonli.javafx.FontIcon;
import tools.sctrade.companion.gui.CardFactory;

/**
 * Factory for building screenshot cards.
 */
public class ScreenshotCardFactory implements CardFactory<Screenshot> {
  private static final int MAX_IMAGE_SIZE = 200;

  private final int imageCacheCapacity;
  private final LinkedHashMap<String, Image> imageByScreenshotId;

  /**
   * Constructor.
   *
   * @param screenshotRepository screenshot repository used to derive cache capacity
   */
  public ScreenshotCardFactory(ScreenshotRepository screenshotRepository) {
    this.imageCacheCapacity = screenshotRepository.getCapacity();
    this.imageByScreenshotId = new LinkedHashMap<>(imageCacheCapacity, 0.75f, true) {
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
        return size() > imageCacheCapacity;
      }
    };
  }

  @Override
  public VBox build(Screenshot screenshot) {
    VBox card = new VBox(5);
    card.getStyleClass().add("screenshot-card");
    card.setMinWidth(240);
    card.setPrefWidth(240);
    card.setMaxWidth(240);

    var title = new javafx.scene.control.Label(screenshot.type().label());
    title.getStyleClass().add("screenshot-card-title");
    title.setMaxWidth(Double.MAX_VALUE);
    title.setAlignment(Pos.CENTER_LEFT);

    VBox header = new VBox(2);
    header.getChildren().add(title);
    header.setFillWidth(true);

    if (screenshot.location() != null) {
      var description = new javafx.scene.control.Label(screenshot.location());
      description.getStyleClass().add("screenshot-card-description");
      description.setWrapText(true);
      description.setMaxWidth(Double.MAX_VALUE);
      description.setAlignment(Pos.CENTER_LEFT);
      header.getChildren().add(description);
    }

    card.getChildren().add(header);

    if (screenshot.image() != null) {
      ImageView imageView = new ImageView(getOrCreateImage(screenshot));
      imageView.setPreserveRatio(true);
      imageView.setFitWidth(MAX_IMAGE_SIZE);
      imageView.setFitHeight(MAX_IMAGE_SIZE);
      card.getChildren().add(imageView);
    }

    card.getChildren().add(createStatusBody(screenshot));
    return card;
  }

  private Image getOrCreateImage(Screenshot screenshot) {
    String id = screenshot.id();
    Image cached = imageByScreenshotId.get(id);
    if (cached != null) {
      return cached;
    }

    BufferedImage scaled = scaleImage(screenshot.image(), MAX_IMAGE_SIZE);
    Image image = SwingFXUtils.toFXImage(scaled, null);
    imageByScreenshotId.put(id, image);
    return image;
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

    FontIcon icon = FontIcon.of(screenshot.status().icon());
    icon.getStyleClass().add("screenshot-card-status-icon");
    icon.getStyleClass().add(screenshot.status().styleClass());
    body.getChildren().add(icon);

    var statusLabel = new javafx.scene.control.Label(getStatusText(screenshot));
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

}
