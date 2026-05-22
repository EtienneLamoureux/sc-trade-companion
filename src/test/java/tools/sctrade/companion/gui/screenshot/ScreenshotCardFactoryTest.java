package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kordamp.ikonli.javafx.FontIcon;
import tools.sctrade.companion.gui.JavaFxTestUtil;

class ScreenshotCardFactoryTest {

  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenScreenshotInput_whenBuildingCard_thenCardContainsExpectedStyle() {
    ScreenshotCardFactory factory = new ScreenshotCardFactory(new ScreenshotRepository());
    Screenshot screenshot = screenshot("id-1", "Area18", ScreenshotStatus.SUCCESS);

    VBox card = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> factory.build(screenshot));

    assertNotNull(card);
    assertTrue(card.getStyleClass().contains("screenshot-card"));
    assertTrue(card.getMaxWidth() > 0);
  }

  @Test
  void givenScreenshotWithoutLocation_whenBuildingCard_thenHeaderHasNoSubtitle() {
    ScreenshotCardFactory factory = new ScreenshotCardFactory(new ScreenshotRepository());
    Screenshot screenshot = screenshot("id-1", null, ScreenshotStatus.SUCCESS);

    VBox header =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> extractHeader(factory.build(screenshot)));

    assertEquals(1, header.getChildren().size());
    assertEquals(Pos.CENTER_LEFT,
        ((javafx.scene.control.Label) header.getChildren().get(0)).getAlignment());
  }

  @Test
  void givenScreenshotInput_whenBuildingCard_thenStatusIconUsesScreenshotMetadata() {
    ScreenshotCardFactory factory = new ScreenshotCardFactory(new ScreenshotRepository());
    Screenshot screenshot = screenshot("id-1", "Area18", ScreenshotStatus.ERROR);

    FontIcon icon =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> extractIcon(factory.build(screenshot)));

    assertTrue(icon.getStyleClass().contains("screenshot-card-status-icon"));
    assertTrue(icon.getStyleClass().contains("screenshot-status-danger"));
  }

  @Test
  void givenSameScreenshotId_whenBuildingTwice_thenImageIsReusedFromCache() {
    ScreenshotCardFactory factory = new ScreenshotCardFactory(new ScreenshotRepository());
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
    ScreenshotCardFactory factory = new ScreenshotCardFactory(repository);
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
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    return new Screenshot(id, image, location, status, null, null, ScreenshotType.COMMODITY_KIOSK);
  }

  private static Image extractImage(VBox card) {
    return card.getChildren().stream().filter(ImageView.class::isInstance)
        .map(ImageView.class::cast).findFirst().orElseThrow().getImage();
  }

  private static FontIcon extractIcon(VBox card) {
    return card.getChildren().stream().filter(VBox.class::isInstance).map(VBox.class::cast)
        .map(vbox -> vbox.getChildren().stream().filter(FontIcon.class::isInstance)
            .map(FontIcon.class::cast).findFirst().orElse(null))
        .filter(java.util.Objects::nonNull).findFirst().orElseThrow();
  }

  private static VBox extractHeader(VBox card) {
    return card.getChildren().stream().filter(VBox.class::isInstance).map(VBox.class::cast)
        .findFirst().orElseThrow();
  }
}
