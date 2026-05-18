package tools.sctrade.companion.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import tools.sctrade.companion.domain.gamelog.GameLogPathSubject;
import tools.sctrade.companion.domain.notification.NotificationLevel;
import tools.sctrade.companion.domain.notification.NotificationRepository;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.gui.version.UpdateAvailablePopup;
import tools.sctrade.companion.utils.LocalizationUtil;
import tools.sctrade.companion.utils.TimeFormat;
import tools.sctrade.companion.utils.TimeUtil;

/**
 * The main GUI class for the companion application.
 */
public class CompanionGui implements NotificationRepository, UpdateAvailablePopup {
  private transient UserService userService;
  private transient GameLogPathSubject gameLogService;
  private transient SettingRepository settings;
  private final String version;
  private Stage stage;
  private LogsTab logsTab;
  private transient Hyperlink activeNavLink;
  private transient FadeTransition currentTransition;

  /**
   * Creates a new instance of the companion GUI.
   *
   * @param userService The user service.
   * @param gameLogService The game log service.
   * @param settings The settings repository.
   * @param version The version of the application.
   */
  public CompanionGui(UserService userService, GameLogPathSubject gameLogService,
      SettingRepository settings, String version) {
    this.userService = userService;
    this.gameLogService = gameLogService;
    this.settings = settings;
    this.version = version;
  }

  /**
   * Initializes the companion GUI stage.
   *
   * @param primaryStage the primary JavaFX stage
   */
  public void initialize(Stage primaryStage) {
    stage = primaryStage;

    stage.setTitle(
        String.format(Locale.ROOT, "%s %s", LocalizationUtil.get("applicationTitle"), version));
    stage.setWidth(600);
    stage.setHeight(575);
    stage.setScene(buildScene());
    stage.getIcons().addAll(Arrays.asList("icon128", "icon64", "icon32", "icon16").stream()
        .map(this::getFxIcon).toList());
    centerStage();
    stage.setOnCloseRequest(event -> {
      Platform.exit();
      System.exit(0);
    });
  }

  @Override
  public void showUpdateAvailablePopup(String currentVersion, String latestVersion) {
    String message = String.format(Locale.ROOT, LocalizationUtil.get("updatePopupMessage"),
        currentVersion, latestVersion);
    ButtonType downloadButton =
        new ButtonType(LocalizationUtil.get("updatePopupDownloadButton"), ButtonData.OK_DONE);
    ButtonType laterButton =
        new ButtonType(LocalizationUtil.get("updatePopupCloseButton"), ButtonData.CANCEL_CLOSE);

    Alert alert = new Alert(Alert.AlertType.INFORMATION, message, downloadButton, laterButton);
    alert.initOwner(stage);
    alert.setTitle(LocalizationUtil.get("updatePopupTitle"));
    alert.setHeaderText(null);

    if (alert.showAndWait().orElse(laterButton) == downloadButton) {
      openReleasePage();
    }
  }

  @Override
  public void add(NotificationLevel level, String message) {
    if (logsTab != null) {
      logsTab.addLog(
          new Object[] {TimeUtil.getNowAsString(TimeFormat.LOG_ENTRY), level.toString(), message});
    }
  }

  private void openReleasePage() {
    if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      add(NotificationLevel.WARN, LocalizationUtil.get("warningUnableToOpenReleasePage"));
      return;
    }

    try {
      Desktop.getDesktop()
          .browse(URI.create("https://github.com/EtienneLamoureux/sc-trade-companion/releases"));
    } catch (IOException e) {
      add(NotificationLevel.WARN, LocalizationUtil.get("warningUnableToOpenReleasePage"));
    }
  }

  private Scene buildScene() {
    logsTab = new LogsTab();
    UsageTab usageTab = new UsageTab();
    SettingsTab settingsTab = new SettingsTab(userService, gameLogService, settings);
    ScreenshotsTab screenshotsTab = new ScreenshotsTab();

    BorderPane root = new BorderPane();
    root.getStyleClass().add("companion-root");
    root.setTop(buildNavBar(root, usageTab, settingsTab, screenshotsTab, logsTab));
    root.setCenter(usageTab);

    Scene scene = new Scene(root, 600, 575);
    scene.getStylesheets().add(getClass().getResource("/styles/companion.css").toExternalForm());
    return scene;
  }

  private HBox buildNavBar(BorderPane root, UsageTab usageTab, SettingsTab settingsTab,
      ScreenshotsTab screenshotsTab, LogsTab logsTab) {
    Hyperlink usageLink = buildNavLink("nav-usage", LocalizationUtil.get("tabUsage"),
        () -> switchCenter(root, usageTab));
    Hyperlink settingsLink = buildNavLink("nav-settings", LocalizationUtil.get("tabSettings"),
        () -> switchCenter(root, settingsTab));
    Hyperlink screenshotsLink = buildNavLink("nav-screenshots",
        LocalizationUtil.get("tabScreenshots"), () -> switchCenter(root, screenshotsTab));
    Hyperlink logsLink = buildNavLink("nav-logs", LocalizationUtil.get("tabLogs"),
        () -> switchCenter(root, logsTab));

    setActiveNavLink(usageLink);
    HBox navBar = new HBox(usageLink, settingsLink, screenshotsLink, logsLink);
    navBar.getStyleClass().add("companion-nav");
    return navBar;
  }

  private Hyperlink buildNavLink(String id, String label, Runnable action) {
    Hyperlink link = new Hyperlink(label);
    link.setId(id);
    link.getStyleClass().add("companion-nav-link");
    link.setOnAction(e -> {
      if (link == activeNavLink) {
        return;
      }
      setActiveNavLink(link);
      action.run();
    });
    return link;
  }

  private void setActiveNavLink(Hyperlink link) {
    if (activeNavLink != null) {
      activeNavLink.getStyleClass().remove("active");
    }
    activeNavLink = link;
    activeNavLink.getStyleClass().add("active");
  }

  private void switchCenter(BorderPane root, Node node) {
    if (node == root.getCenter()) {
      return;
    }
    if (currentTransition != null) {
      currentTransition.stop();
    }
    node.setOpacity(0);
    root.setCenter(node);
    currentTransition = new FadeTransition(Duration.millis(150), node);
    currentTransition.setFromValue(0);
    currentTransition.setToValue(1);
    currentTransition.setOnFinished(e -> currentTransition = null);
    currentTransition.play();
  }

  private void centerStage() {
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    stage.setX((bounds.getWidth() - stage.getWidth()) / 2);
    stage.setY((bounds.getHeight() - stage.getHeight()) / 2);
  }

  private javafx.scene.image.Image getFxIcon(String name) {
    return new javafx.scene.image.Image(
        getClass().getResourceAsStream(String.format(Locale.ROOT, "/images/icons/%s.png", name)));
  }
}
