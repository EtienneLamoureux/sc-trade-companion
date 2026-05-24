package tools.sctrade.companion.gui.screenshot;

import atlantafx.base.controls.Tile;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.imgscalr.Scalr;
import tools.sctrade.companion.gui.TileFactory;

/**
 * Factory for building screenshot tiles.
 */
public class ScreenshotTileFactory implements TileFactory<Screenshot> {
  private static final int MAX_IMAGE_SIZE = 150;

  private final int imageCacheCapacity;
  private final LinkedHashMap<String, Image> imageByScreenshotId;

  /**
   * Constructor.
   *
   * @param screenshotRepository screenshot repository used to derive cache capacity
   */
  public ScreenshotTileFactory(ScreenshotRepository screenshotRepository) {
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
  public Tile build(Screenshot screenshot) {
    Tile tile = new Tile(screenshot.type().label(), createDescription(screenshot));
    tile.getStyleClass().add("screenshot-tile");
    tile.setGraphic(createGraphic(screenshot));
    return tile;
  }

  private ImageView createGraphic(Screenshot screenshot) {
    if (screenshot.image() == null) {
      return null;
    }

    ImageView imageView = new ImageView(getOrCreateImage(screenshot));
    imageView.setFitWidth(MAX_IMAGE_SIZE);
    imageView.setFitHeight(MAX_IMAGE_SIZE);
    imageView.setPreserveRatio(true);
    return imageView;
  }

  private String createDescription(Screenshot screenshot) {
    ScreenshotStatus status = screenshot.status();
    String statusText = switch (status) {
      case SUCCESS -> screenshot.content() != null ? screenshot.content() : status.defaultText();
      case ERROR -> screenshot.error() != null ? screenshot.error() : status.defaultText();
      default -> status.defaultText();
    };

    String glyph = status.glyph();

    return "[span class=\"screenshot-tile-status " + status.styleClass() + "\"]" + glyph + " "
        + sanitizeBbCodeText(statusText) + "[/span]";
  }

  private String sanitizeBbCodeText(String text) {
    return text.replace("[", "［").replace("]", "］");
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
}
