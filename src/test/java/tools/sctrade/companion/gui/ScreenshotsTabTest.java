package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.util.List;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.gui.screenshot.Screenshot;
import tools.sctrade.companion.gui.screenshot.ScreenshotRepository;
import tools.sctrade.companion.gui.screenshot.ScreenshotStatus;
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
  void givenEmptyRepositoryWhenRenderedThenDisplaysNoCards() {
    ScreenshotRepository repository = new ScreenshotRepository();
    ScreenshotsTab tab =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> new ScreenshotsTab(repository));
    int cardCount = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getCardCount(tab));
    assertEquals(0, cardCount);
  }

  @Test
  void givenRepositoryWithOneScreenshotWhenRenderedThenDisplaysOneCard() {
    ScreenshotRepository repository = new ScreenshotRepository();
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Screenshot screenshot = new Screenshot("id-1", image, "Commodity Exchange",
        ScreenshotStatus.SUCCESS, null, null, ScreenshotType.COMMODITY_KIOSK);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    int cardCount = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getCardCount(tab));
    assertEquals(1, cardCount);
  }

  @Test
  void givenRepositoryWithMultipleScreenshotsWhenRenderedThenCardsAreInNewestFirstOrder() {
    ScreenshotRepository repository = new ScreenshotRepository();
    BufferedImage image1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage image2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage image3 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

    Screenshot old = new Screenshot("id-old", image1, "Location A", ScreenshotStatus.SUCCESS, null,
        null, ScreenshotType.COMMODITY_KIOSK);
    Screenshot middle = new Screenshot("id-middle", image2, "Location B", ScreenshotStatus.SUCCESS,
        null, null, ScreenshotType.ITEM_KIOSK);
    Screenshot newest = new Screenshot("id-newest", image3, "Location C", ScreenshotStatus.SUCCESS,
        null, null, ScreenshotType.COMMODITY_KIOSK);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(old);
      repository.upsert(middle);
      repository.upsert(newest);
      return new ScreenshotsTab(repository);
    });

    List<String> titles = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getCardTitles(tab));
    assertEquals(3, titles.size());
    assertEquals("Commodity kiosk", titles.get(0)); // newest
    assertEquals("Item kiosk", titles.get(1)); // middle
    assertEquals("Commodity kiosk", titles.get(2)); // old
  }

  @Test
  void givenCardWhenStatusIsSuccessThenDisplaysLocationInBody() {
    ScreenshotRepository repository = new ScreenshotRepository();
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Screenshot screenshot = new Screenshot("id-1", image, "Test Location", ScreenshotStatus.SUCCESS,
        null, null, ScreenshotType.COMMODITY_KIOSK);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    String statusText = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getCardStatusText(tab, 0));
    assertEquals("Test Location", statusText);
  }

  @Test
  void givenCardWhenStatusIsQueuedThenDisplaysInQueueMessage() {
    ScreenshotRepository repository = new ScreenshotRepository();
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Screenshot screenshot = new Screenshot("id-1", image, null, ScreenshotStatus.QUEUED, null, null,
        ScreenshotType.COMMODITY_KIOSK);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    String statusText = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getCardStatusText(tab, 0));
    assertEquals("In queue", statusText);
  }

  @Test
  void givenCardWhenStatusIsProcessingThenDisplaysProcessingMessage() {
    ScreenshotRepository repository = new ScreenshotRepository();
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Screenshot screenshot = new Screenshot("id-1", image, null, ScreenshotStatus.PROCESSING, null,
        null, ScreenshotType.COMMODITY_KIOSK);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    String statusText = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getCardStatusText(tab, 0));
    assertEquals("Processing...", statusText);
  }

  @Test
  void givenCardWhenStatusIsErrorThenDisplaysErrorMessage() {
    ScreenshotRepository repository = new ScreenshotRepository();
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Screenshot screenshot = new Screenshot("id-1", image, null, ScreenshotStatus.ERROR,
        "Image too small", null, ScreenshotType.COMMODITY_KIOSK);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    String statusText = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getCardStatusText(tab, 0));
    assertEquals("Image too small", statusText);
  }

  @Test
  void givenCardWhenLocationNotAvailableThenShowsEllipsis() {
    ScreenshotRepository repository = new ScreenshotRepository();
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Screenshot screenshot = new Screenshot("id-1", image, null, ScreenshotStatus.SUCCESS, null,
        null, ScreenshotType.COMMODITY_KIOSK);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    String description = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getCardDescription(tab, 0));
    assertEquals("...", description);
  }

  @Test
  void givenCardWhenImageIsNullThenStillRendersCard() {
    ScreenshotRepository repository = new ScreenshotRepository();
    Screenshot screenshot = new Screenshot("id-1", null, "Test Location", ScreenshotStatus.SUCCESS,
        null, null, ScreenshotType.COMMODITY_KIOSK);

    ScreenshotsTab tab = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      repository.upsert(screenshot);
      return new ScreenshotsTab(repository);
    });

    int cardCount = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> getCardCount(tab));
    assertEquals(1, cardCount);
  }

  @Test
  void givenTabWhenConstructedThenHasGridPaneLayout() {
    ScreenshotRepository repository = new ScreenshotRepository();
    ScreenshotsTab tab =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> new ScreenshotsTab(repository));
    GridPane grid = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> (GridPane) tab.getCenter());
    assertNotNull(grid);
  }

  private int getCardCount(ScreenshotsTab tab) {
    GridPane grid = (GridPane) tab.getCenter();
    return grid == null ? 0 : grid.getChildren().size();
  }

  private List<String> getCardTitles(ScreenshotsTab tab) {
    GridPane grid = (GridPane) tab.getCenter();
    if (grid == null || grid.getChildren().isEmpty()) {
      return java.util.Collections.emptyList();
    }

    var titles = new java.util.ArrayList<String>();
    for (var child : grid.getChildren()) {
      if (child instanceof javafx.scene.layout.VBox vbox) {
        if (!vbox.getChildren().isEmpty()
            && vbox.getChildren().get(0) instanceof javafx.scene.layout.BorderPane header) {
          if (header.getCenter() instanceof javafx.scene.control.Label titleLabel) {
            titles.add(titleLabel.getText());
          }
        }
      }
    }
    return titles;
  }

  private String getCardStatusText(ScreenshotsTab tab, int cardIndex) {
    GridPane grid = (GridPane) tab.getCenter();
    if (grid == null || cardIndex >= grid.getChildren().size()) {
      return "";
    }

    var card = grid.getChildren().get(cardIndex);
    if (card instanceof javafx.scene.layout.VBox vbox && vbox.getChildren().size() > 2) {
      var statusBody = vbox.getChildren().get(vbox.getChildren().size() - 1);
      if (statusBody instanceof javafx.scene.layout.VBox statusVBox) {
        for (var child : statusVBox.getChildren()) {
          if (child instanceof javafx.scene.control.Label label
              && !label.getStyleClass().contains("screenshot-card-status-icon")) {
            return label.getText();
          }
        }
      }
    }
    return "";
  }

  private String getCardDescription(ScreenshotsTab tab, int cardIndex) {
    GridPane grid = (GridPane) tab.getCenter();
    if (grid == null || cardIndex >= grid.getChildren().size()) {
      return "";
    }

    var card = grid.getChildren().get(cardIndex);
    if (card instanceof javafx.scene.layout.VBox vbox && !vbox.getChildren().isEmpty()) {
      if (vbox.getChildren().get(0) instanceof javafx.scene.layout.BorderPane header) {
        if (header.getBottom() instanceof javafx.scene.control.Label descLabel) {
          return descLabel.getText();
        }
      }
    }
    return "";
  }
}
