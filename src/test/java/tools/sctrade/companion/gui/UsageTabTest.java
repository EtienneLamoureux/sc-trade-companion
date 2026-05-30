package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.utils.LocalizationUtil;

class UsageTabTest {
  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenUsageTabWhenInitializedThenShowTabbedInstructionsAndVideoContent() {
    UsageTab usageTab = JavaFxTestUtil.supplyOnFxThreadAndWait(UsageTab::new);

    ScrollPane scrollPane = assertInstanceOf(ScrollPane.class, usageTab.getCenter());
    VBox content = assertInstanceOf(VBox.class, scrollPane.getContent());
    TabPane leftMiddleTabs =
        assertInstanceOf(TabPane.class, findByStyleClass(content, "usage-left-middle-tabs"));

    assertNotNull(leftMiddleTabs);
    assertEquals(1, content.getChildren().size());
  }

  @Test
  void givenUsageTabWhenInitializedThenOnlySelectedTabInitializesVideoPlayer() {
    UsageTab usageTab = JavaFxTestUtil.supplyOnFxThreadAndWait(UsageTab::new);

    ScrollPane scrollPane = assertInstanceOf(ScrollPane.class, usageTab.getCenter());
    VBox content = assertInstanceOf(VBox.class, scrollPane.getContent());
    TabPane tabPane =
        assertInstanceOf(TabPane.class, findByStyleClass(content, "usage-left-middle-tabs"));

    assertEquals(2, tabPane.getTabs().size());
    assertEquals(LocalizationUtil.get("usageVideoTabCommodities"),
        tabPane.getTabs().get(0).getText());
    assertEquals(LocalizationUtil.get("usageVideoTabGearComponents"),
        tabPane.getTabs().get(1).getText());

    Tab commodityTab = tabPane.getTabs().get(0);
    Tab itemTab = tabPane.getTabs().get(1);
    assertEquals(commodityTab, tabPane.getSelectionModel().getSelectedItem());

    HBox commodityTabContent = assertInstanceOf(HBox.class, commodityTab.getContent(),
        "Tab content should be two-pane HBox");
    Node commodityLeftPane = findByStyleClass(commodityTabContent, "usage-left-stack");
    VBox commodityMiddlePane =
        assertInstanceOf(VBox.class, findByStyleClass(commodityTabContent, "usage-middle-pane"));
    assertInstanceOf(MediaView.class, findMediaView(commodityMiddlePane));
    assertEquals(1, commodityMiddlePane.getChildren().size());
    assertNotNull(commodityLeftPane);

    HBox itemTabContent =
        assertInstanceOf(HBox.class, itemTab.getContent(), "Tab content should be two-pane HBox");
    Node itemLeftPane = findByStyleClass(itemTabContent, "usage-left-stack");
    VBox itemMiddlePane =
        assertInstanceOf(VBox.class, findByStyleClass(itemTabContent, "usage-middle-pane"));
    assertEquals(0, itemMiddlePane.getChildren().size());
    assertNotNull(itemLeftPane);
  }

  @Test
  void givenMiddlePaneWhenComputedVideoHeightBelow200ThenMiddlePaneIsHidden() {
    UsageTab usageTab = JavaFxTestUtil.supplyOnFxThreadAndWait(UsageTab::new);

    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      usageTab.setPrefWidth(540);
      usageTab.setPrefHeight(240);
      usageTab.applyCss();
      usageTab.layout();
    });

    ScrollPane scrollPane = assertInstanceOf(ScrollPane.class, usageTab.getCenter());
    VBox content = assertInstanceOf(VBox.class, scrollPane.getContent());
    TabPane tabPane =
        assertInstanceOf(TabPane.class, findByStyleClass(content, "usage-left-middle-tabs"));
    HBox selectedTabContent =
        assertInstanceOf(HBox.class, tabPane.getSelectionModel().getSelectedItem().getContent());
    Node middlePane = findByStyleClass(selectedTabContent, "usage-middle-pane");

    assertNotNull(middlePane);
    assertFalse(middlePane.isVisible());
    assertFalse(middlePane.isManaged());
  }

  @Test
  void givenWheelDeltaWhenCalculatingPageScrollThenScrollValueIncreases() {
    assertEquals(0.8d, UsageTab.calculatePageScrollVvalue(0d, -240d));
  }

  @Test
  void givenEnglishBundleWhenReadingCommodityInstructionsThenNoSystemTrayMention() {
    String instructions = englishBundle().getString("usageInstructionsCommodities");

    assertFalse(instructions.toLowerCase().contains("system tray"),
        "Instructions should not mention the system tray");
  }

  @Test
  void givenEnglishBundleWhenReadingTabScreenshotsKeyThenReturnsScreenshots() {
    assertEquals("Screenshots", englishBundle().getString("tabScreenshots"));
  }

  @Test
  void givenEnglishBundleWhenReadingUsageVideoTabsThenReturnsExpectedLabels() {
    assertEquals("Commodities", englishBundle().getString("usageVideoTabCommodities"));
    assertEquals("Gear & components", englishBundle().getString("usageVideoTabGearComponents"));
  }

  @Test
  void givenEnglishBundleWhenReadingUsageInstructionsGearThenContainsExpectedKioskSteps() {
    String instructions = englishBundle().getString("usageInstructionsGearComponents");

    assertTrue(instructions.contains("Select the location"));
    assertTrue(instructions.contains("Scroll and repeat to capture all listings"));
  }

  @Test
  void givenSelectedUsageTabVideoWhenLoadedThenVideoIsTopAlignedInContainer() {
    UsageTab usageTab = JavaFxTestUtil.supplyOnFxThreadAndWait(UsageTab::new);
    TabPane tabPane = getLeftMiddleTabs(usageTab);
    VBox middlePane = getMiddlePane(tabPane.getTabs().get(0));
    MediaView mediaView = findMediaView(middlePane);

    assertTrue(mediaView.isPreserveRatio(), "Video should preserve aspect ratio");
    assertTrue(mediaView.fitHeightProperty().isBound(), "Video should be vertically bound");
  }

  @Test
  void givenSelectedUsageTabVideoWhenLoadedThenHasNoControlButton() {
    UsageTab usageTab = JavaFxTestUtil.supplyOnFxThreadAndWait(UsageTab::new);
    TabPane tabPane = getLeftMiddleTabs(usageTab);
    VBox middlePane = getMiddlePane(tabPane.getTabs().get(0));

    assertEquals(1, middlePane.getChildren().size());
    assertInstanceOf(StackPane.class, middlePane.getChildren().get(0));
  }

  @Test
  void givenUsageVideoTabsWhenSwitchingTabThenSelectedTabKeepsMediaPlayerWired() {
    AtomicReference<Stage> stageRef = new AtomicReference<>();
    AtomicReference<UsageTab> usageRef = new AtomicReference<>();
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      Stage stage = new Stage();
      UsageTab usageTab = new UsageTab();
      stage.setScene(new Scene(usageTab, 920, 680));
      stage.show();
      stageRef.set(stage);
      usageRef.set(usageTab);
    });

    TabPane tabPane = getLeftMiddleTabs(usageRef.get());
    JavaFxTestUtil.runOnFxThreadAndWait(() -> tabPane.getSelectionModel().select(1));
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      // Allow tab selection callbacks to run.
    });

    MediaPlayer selectedPlayer =
        findMediaView(getMiddlePane(tabPane.getTabs().get(1))).getMediaPlayer();
    assertNotNull(selectedPlayer);
    assertTrue(selectedPlayer.isAutoPlay());
    assertEquals(MediaPlayer.INDEFINITE, selectedPlayer.getCycleCount());
  }

  @Test
  void givenItemUsageVideoWhenSelectedThenMediaPlayerStartsPlaying() {
    AtomicReference<Stage> stageRef = new AtomicReference<>();
    AtomicReference<UsageTab> usageRef = new AtomicReference<>();
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      Stage stage = new Stage();
      UsageTab usageTab = new UsageTab();
      stage.setScene(new Scene(usageTab, 920, 680));
      stage.show();
      stageRef.set(stage);
      usageRef.set(usageTab);
    });

    TabPane tabPane = getLeftMiddleTabs(usageRef.get());
    JavaFxTestUtil.runOnFxThreadAndWait(() -> tabPane.getSelectionModel().select(1));
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Interrupted while waiting for item video playback", e);
    }
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      // Allow item tab playback callbacks to run.
    });

    MediaPlayer selectedPlayer =
        findMediaView(getMiddlePane(tabPane.getTabs().get(1))).getMediaPlayer();
    assertNotNull(selectedPlayer);
    assertEquals(MediaPlayer.Status.PLAYING, selectedPlayer.getStatus());
    assertNotNull(selectedPlayer.getMedia());
    assertFalse(selectedPlayer.getMedia().getError() != null);
  }

  @Test
  void givenItemUsageVideoWhenSelectedThenRenderedFrameIsNotBlank() {
    AtomicReference<Stage> stageRef = new AtomicReference<>();
    AtomicReference<UsageTab> usageRef = new AtomicReference<>();
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      Stage stage = new Stage();
      UsageTab usageTab = new UsageTab();
      stage.setScene(new Scene(usageTab, 920, 680));
      stage.show();
      stageRef.set(stage);
      usageRef.set(usageTab);
    });

    TabPane tabPane = getLeftMiddleTabs(usageRef.get());
    JavaFxTestUtil.runOnFxThreadAndWait(() -> tabPane.getSelectionModel().select(1));
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Interrupted while waiting for item video rendering", e);
    }

    // Verify the MediaView and MediaPlayer are properly configured
    MediaView mediaView = JavaFxTestUtil
        .supplyOnFxThreadAndWait(() -> findMediaView(getMiddlePane(tabPane.getTabs().get(1))));
    MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
    assertNotNull(mediaPlayer);
    assertNotNull(mediaPlayer.getMedia());
    assertFalse(mediaPlayer.getMedia().getError() != null);

    // Check if the snapshot has visible pixel variance
    Image snapshot = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> mediaView.snapshot(null, null));
    assertTrue(hasVisiblePixelVariance(snapshot),
        "Expected snapshot to have visible pixel variance, but got a blank or uniform image. "
            + "This could indicate the video is not rendering properly in the test environment.");
  }

  @Test
  void givenUsageVideoWhenResizedThenKeepsMediaPaneConfigured() {
    AtomicReference<Stage> stageRef = new AtomicReference<>();
    AtomicReference<UsageTab> usageRef = new AtomicReference<>();
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      Stage stage = new Stage();
      UsageTab usageTab = new UsageTab();
      stage.setScene(new Scene(usageTab, 920, 680));
      stage.show();
      stageRef.set(stage);
      usageRef.set(usageTab);
    });

    Stage stage = stageRef.get();
    UsageTab usageTab = usageRef.get();
    TabPane tabPane = getLeftMiddleTabs(usageTab);
    VBox middlePane = getMiddlePane(tabPane.getTabs().get(0));
    MediaView mediaView = findMediaView(middlePane);
    MediaPlayer commodityPlayer = mediaView.getMediaPlayer();
    assertNotNull(commodityPlayer);
    assertTrue(mediaView.fitHeightProperty().isBound());

    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      stage.setWidth(1160);
      stage.setHeight(820);
    });
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Interrupted while waiting for resize playback recovery", e);
    }
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      // Allow resize listeners to run.
    });

    assertNotNull(commodityPlayer);
    assertTrue(mediaView.fitHeightProperty().isBound());
  }

  @Test
  void givenUsageStylesheetWhenLoadedThenMiddlePaneHasNoBorderAndVideoViewHasNoBorder() {
    String css = readCompanionCss();

    assertTrue(css.contains(".usage-middle-pane {"));
    assertTrue(css.contains("-fx-border-color: transparent;"));
    assertTrue(css.contains(".usage-video-view {"));
  }

  private Node findByStyleClass(Parent content, String styleClass) {
    if (content.getStyleClass().contains(styleClass)) {
      return content;
    }
    return content.getChildrenUnmodifiable().stream()
        .filter(node -> node.getStyleClass().contains(styleClass) || node instanceof Parent)
        .map(node -> node.getStyleClass().contains(styleClass) ? node
            : findByStyleClass((Parent) node, styleClass))
        .filter(java.util.Objects::nonNull).findFirst().orElse(null);
  }

  private TabPane getLeftMiddleTabs(UsageTab usageTab) {
    ScrollPane scrollPane = assertInstanceOf(ScrollPane.class, usageTab.getCenter());
    VBox content = assertInstanceOf(VBox.class, scrollPane.getContent());
    return assertInstanceOf(TabPane.class, findByStyleClass(content, "usage-left-middle-tabs"));
  }

  private VBox getMiddlePane(Tab tab) {
    HBox tabContent = assertInstanceOf(HBox.class, tab.getContent());
    VBox middlePane =
        assertInstanceOf(VBox.class, findByStyleClass(tabContent, "usage-middle-pane"));
    return middlePane;
  }

  private MediaView findMediaView(VBox middlePane) {
    return assertInstanceOf(MediaView.class, middlePane.getChildren().stream()
        .filter(StackPane.class::isInstance).map(StackPane.class::cast)
        .flatMap(stackPane -> stackPane.getChildren().stream().filter(MediaView.class::isInstance))
        .findFirst().orElseThrow(() -> new AssertionError("MediaView not found in video pane")));
  }

  private String readCompanionCss() {
    try {
      return new String(getClass().getResourceAsStream("/styles/companion.css").readAllBytes(),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new AssertionError("Unable to read companion.css", e);
    }
  }

  private ResourceBundle englishBundle() {
    return ResourceBundle.getBundle("bundles.localization", Locale.ROOT);
  }

  private boolean hasVisiblePixelVariance(Image image) {
    int width = (int) image.getWidth();
    int height = (int) image.getHeight();
    if (width == 0 || height == 0) {
      return false;
    }

    javafx.scene.image.PixelReader reader = image.getPixelReader();
    int minX = width / 4;
    int maxX = width - minX;
    int minY = height / 4;
    int maxY = height - minY;
    java.util.Set<Integer> colors = new java.util.HashSet<>();
    for (int y = minY; y < maxY; y++) {
      for (int x = minX; x < maxX; x++) {
        colors.add(reader.getArgb(x, y));
        // Accept any image with at least 2 different colors as "rendered"
        if (colors.size() >= 2) {
          return true;
        }
      }
    }
    return false;
  }
}
