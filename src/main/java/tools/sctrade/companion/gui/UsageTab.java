package tools.sctrade.companion.gui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * The usage tab for the companion GUI. This is where users can see the instructions on how to use
 * this app.
 */
public class UsageTab extends BorderPane {
  private static final double CONTENT_PADDING = 16d;
  private static final double CONTENT_SPACING = 16d;
  private static final double SIDE_PANE_WIDTH = 320d;
  private static final double VIDEO_ASPECT_RATIO = 9d / 16d;
  private static final double MIN_MIDDLE_VIDEO_HEIGHT = 200d;
  private final ScrollPane pageScrollPane;

  /**
   * Creates a new instance of the usage tab.
   */
  public UsageTab() {
    pageScrollPane = new ScrollPane();

    DoubleBinding availableWidth = Bindings.createDoubleBinding(
        () -> Math.max(0d, getWidth() - (2 * CONTENT_PADDING) - CONTENT_SPACING - SIDE_PANE_WIDTH),
        widthProperty());
    DoubleBinding availableHeight = Bindings.createDoubleBinding(
        () -> Math.max(0d, getHeight() - (2 * CONTENT_PADDING)), heightProperty());
    DoubleBinding computedVideoHeight = Bindings.createDoubleBinding(
        () -> Math.min(availableHeight.get(), availableWidth.get() * VIDEO_ASPECT_RATIO),
        availableHeight, availableWidth);

    TabPane leftMiddleTabs = createLeftMiddleTabs(computedVideoHeight, pageScrollPane);
    leftMiddleTabs.getStyleClass().add("usage-left-middle-tabs");
    leftMiddleTabs.setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(leftMiddleTabs, Priority.ALWAYS);
    installResizePlaybackRecovery(leftMiddleTabs);

    VBox content = new VBox(leftMiddleTabs);
    content.getStyleClass().add("usage-content");

    pageScrollPane.setContent(content);
    pageScrollPane.getStyleClass().add("usage-scroll-pane");
    pageScrollPane.setPannable(true);
    pageScrollPane.setFitToHeight(false);
    pageScrollPane.setFitToWidth(true);
    setCenter(pageScrollPane);
  }

  private TabPane createLeftMiddleTabs(DoubleBinding computedVideoHeight,
      ScrollPane pageScrollPane) {
    Tab commodityTab = new Tab(LocalizationUtil.get("usageVideoTabCommodities"),
        createTabContent("usageInstructionsCommodities", "/videos/example-kiosk-commodity.mp4",
            computedVideoHeight, pageScrollPane));
    commodityTab.setClosable(false);

    Tab itemTab = new Tab(LocalizationUtil.get("usageVideoTabGearComponents"),
        createTabContent("usageInstructionsGearComponents", "/videos/example-kiosk-item.mp4",
            computedVideoHeight, pageScrollPane));
    itemTab.setClosable(false);

    TabPane tabPane = new TabPane(commodityTab, itemTab);
    tabPane.getStyleClass().add("usage-video-tab-pane");
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
      if (newTab != null) {
        if (oldTab != null) {
          pauseVideoPlayback(oldTab);
        }
        refreshVideoPlayback(newTab);
        Platform.runLater(() -> refreshVideoPlayback(newTab));
      }
    });
    refreshVideoPlayback(commodityTab);
    return tabPane;
  }

  private HBox createTabContent(String instructionKey, String videoPath,
      DoubleBinding computedVideoHeight, ScrollPane pageScrollPane) {
    WebView instructions = createInstructionsPane(instructionKey, pageScrollPane);

    VBox leftPane = new VBox(12, instructions);
    leftPane.getStyleClass().add("usage-left-stack");
    leftPane.setPrefWidth(SIDE_PANE_WIDTH);
    leftPane.setMinWidth(240d);

    VBox middlePane = createVideoPane(videoPath);
    middlePane.getStyleClass().add("usage-middle-pane");
    HBox.setHgrow(middlePane, Priority.ALWAYS);
    middlePane.visibleProperty()
        .bind(computedVideoHeight.greaterThanOrEqualTo(MIN_MIDDLE_VIDEO_HEIGHT));
    middlePane.managedProperty().bind(middlePane.visibleProperty());

    HBox tabContent = new HBox(CONTENT_SPACING, leftPane, middlePane);
    tabContent.getStyleClass().add("usage-tab-content");
    return tabContent;
  }

  private WebView createInstructionsPane(String instructionKey, ScrollPane pageScrollPane) {
    WebView instructions = createTextPane(instructionKey, pageScrollPane);
    instructions.getStyleClass().add("usage-left-pane");
    instructions.setPrefWidth(SIDE_PANE_WIDTH);
    instructions.setMinWidth(240d);
    return instructions;
  }

  private WebView createTextPane(String contentKey, ScrollPane pageScrollPane) {
    WebView textView = new WebView();
    textView.getEngine().loadContent(wrapNoScrollHtml(LocalizationUtil.get(contentKey)));
    textView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
      if (newState != javafx.concurrent.Worker.State.SUCCEEDED) {
        return;
      }
      Object heightValue = textView.getEngine().executeScript(
          "Math.max(document.body.scrollHeight, document.documentElement.scrollHeight)");
      if (heightValue instanceof Number numberHeight) {
        textView.setPrefHeight(Math.max(320d, numberHeight.doubleValue()));
      }
    });
    installOuterScrollRelay(textView, pageScrollPane);
    return textView;
  }

  private void installOuterScrollRelay(WebView textView, ScrollPane pageScrollPane) {
    textView.addEventFilter(ScrollEvent.SCROLL, event -> {
      if (pageScrollPane.getContent() == null) {
        return;
      }

      pageScrollPane
          .setVvalue(calculatePageScrollVvalue(pageScrollPane.getVvalue(), event.getDeltaY()));
      event.consume();
    });
  }

  static double calculatePageScrollVvalue(double currentVvalue, double deltaY) {
    return clamp(currentVvalue - (deltaY / 300d));
  }

  private static double clamp(double value) {
    return Math.max(0d, Math.min(1d, value));
  }

  private String wrapNoScrollHtml(String htmlContent) {
    String wrapped = CompanionTheme.wrapInstructionsHtml(htmlContent);
    return wrapped.replace("</head>", """
        <style>
          html, body {
            overflow: hidden !important;
          }
          ::-webkit-scrollbar {
            display: none;
          }
        </style>
        </head>
        """);
  }

  private VBox createVideoPane(String videoPath) {
    MediaPlayer mediaPlayer =
        new MediaPlayer(new Media(getClass().getResource(videoPath).toExternalForm()));
    mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
    mediaPlayer.setOnEndOfMedia(() -> {
      mediaPlayer.seek(Duration.ZERO);
      mediaPlayer.play();
    });

    MediaView mediaView = new MediaView(mediaPlayer);
    mediaView.getStyleClass().add("usage-video-view");
    mediaView.setPreserveRatio(true);
    mediaView.setSmooth(true);

    mediaPlayer.setOnReady(() -> {
      mediaPlayer.play();
    });
    mediaPlayer.setAutoPlay(true);

    StackPane mediaSurface = new StackPane(mediaView);
    StackPane.setAlignment(mediaView, Pos.TOP_CENTER);
    mediaView.fitWidthProperty().bind(mediaSurface.widthProperty());
    mediaView.fitHeightProperty().bind(mediaSurface.heightProperty());

    VBox videoPane = new VBox(mediaSurface);
    videoPane.setAlignment(Pos.TOP_CENTER);
    VBox.setVgrow(mediaSurface, Priority.ALWAYS);
    return videoPane;
  }

  private void triggerVideoPlayback(Tab tab) {
    refreshVideoPlayback(tab);
  }

  private void refreshVideoPlayback(Tab tab) {
    if (!(tab.getContent() instanceof HBox tabContent)) {
      return;
    }
    tabContent.getChildren().stream()
        .filter(node -> node.getStyleClass().contains("usage-middle-pane")).findFirst()
        .filter(VBox.class::isInstance).map(VBox.class::cast).ifPresent(middlePane -> {
          MediaView mediaView = findMediaView(middlePane);
          MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
          if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            mediaPlayer.play();
          }
        });
  }

  private void installResizePlaybackRecovery(TabPane tabPane) {
    ChangeListener<Number> listener = (obs, oldValue, newValue) -> {
      Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
      if (selectedTab == null) {
        return;
      }
      refreshVideoPlayback(selectedTab);
      Platform.runLater(() -> refreshVideoPlayback(selectedTab));
    };
    widthProperty().addListener(listener);
    heightProperty().addListener(listener);
    tabPane.widthProperty().addListener(listener);
    tabPane.heightProperty().addListener(listener);
  }

  private void pauseVideoPlayback(Tab tab) {
    if (!(tab.getContent() instanceof HBox tabContent)) {
      return;
    }
    tabContent.getChildren().stream()
        .filter(node -> node.getStyleClass().contains("usage-middle-pane")).findFirst()
        .filter(VBox.class::isInstance).map(VBox.class::cast).ifPresent(middlePane -> {
          MediaView mediaView = findMediaView(middlePane);
          MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
          if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
          }
        });
  }

  private MediaView findMediaView(VBox middlePane) {
    return middlePane.getChildren().stream().filter(StackPane.class::isInstance)
        .map(StackPane.class::cast).findFirst()
        .flatMap(stackPane -> stackPane.getChildren().stream().filter(MediaView.class::isInstance)
            .map(MediaView.class::cast).findFirst())
        .orElseThrow(() -> new IllegalStateException("MediaView not found in video pane"));
  }

}
