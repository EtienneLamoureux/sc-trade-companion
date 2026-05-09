package tools.sctrade.companion.gui;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
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
    Platform.setImplicitExit(false);

    stage.setTitle(
        String.format(Locale.ROOT, "%s %s", LocalizationUtil.get("applicationTitle"), version));
    stage.setWidth(600);
    stage.setHeight(575);
    stage.setScene(buildScene());
    stage.getIcons().addAll(Arrays.asList("icon128", "icon64", "icon32", "icon16").stream()
        .map(this::getFxIcon).toList());
    centerStage();
    setupTray();
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

    VBox root = new VBox();
    root.getStyleClass().add("companion-root");
    root.getChildren().add(buildMenuBar());

    BorderPane content = new BorderPane();
    content.getStyleClass().add("companion-content");
    content.setCenter(buildTabs());
    VBox.setVgrow(content, Priority.ALWAYS);
    root.getChildren().add(content);

    Scene scene = new Scene(root, 600, 575);
    scene.getStylesheets().add(getClass().getResource("/styles/companion.css").toExternalForm());
    return scene;
  }

  private void centerStage() {
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    stage.setX((bounds.getWidth() - stage.getWidth()) / 2);
    stage.setY((bounds.getHeight() - stage.getHeight()) / 2);
  }

  private MenuBar buildMenuBar() {
    var menuBar = new MenuBar();
    menuBar.getMenus().add(buildFileMenu());
    return menuBar;
  }

  private Menu buildFileMenu() {
    var fileMenu = new Menu(LocalizationUtil.get("menuFile"));

    javafx.scene.control.MenuItem closeMenuItem =
        new javafx.scene.control.MenuItem(LocalizationUtil.get("menuItemSendToTray"));
    closeMenuItem.setOnAction(event -> stage.hide());
    fileMenu.getItems().add(closeMenuItem);

    javafx.scene.control.MenuItem exitMenuItem =
        new javafx.scene.control.MenuItem(LocalizationUtil.get("menuItemExit"));
    exitMenuItem.setOnAction(event -> {
      Platform.exit();
      System.exit(0);
    });
    fileMenu.getItems().add(exitMenuItem);
    return fileMenu;
  }

  private TabPane buildTabs() {
    var tabbedPane = new TabPane();
    tabbedPane.getStyleClass().add("companion-tabs");
    tabbedPane.getTabs().add(buildTab(LocalizationUtil.get("tabUsage"), new UsageTab()));
    tabbedPane.getTabs().add(buildTab(LocalizationUtil.get("tabSettings"),
        new SettingsTab(userService, gameLogService, settings)));
    tabbedPane.getTabs().add(buildTab(LocalizationUtil.get("tabLogs"), logsTab));
    return tabbedPane;
  }

  private Tab buildTab(String title, javafx.scene.Node content) {
    Tab tab = new Tab(title, content);
    tab.setClosable(false);
    return tab;
  }

  private void setupTray() {
    if (SystemTray.isSupported()) {
      PopupMenu popupMenu = new PopupMenu();
      popupMenu.add(buildOpenMenuItem());
      popupMenu.add(buildExitMenuItem());

      TrayIcon trayIcon = new TrayIcon(getIcon("icon16"));
      trayIcon.setPopupMenu(popupMenu);
      trayIcon.setImageAutoSize(true);
      trayIcon.setToolTip(LocalizationUtil.get("applicationTitle"));

      try {
        SystemTray systemTray = SystemTray.getSystemTray();
        systemTray.add(trayIcon);
      } catch (Exception e) {
        throw new IllegalStateException("Unable to initialize the system tray", e);
      }
    }
  }

  private MenuItem buildOpenMenuItem() {
    MenuItem openMenuItem = new MenuItem(LocalizationUtil.get("menuItemOpen"));
    openMenuItem.addActionListener(e -> Platform.runLater(() -> {
      stage.show();
      stage.toFront();
    }));

    return openMenuItem;
  }

  private MenuItem buildExitMenuItem() {
    MenuItem exitMenuItem = new MenuItem(LocalizationUtil.get("menuItemExit"));
    exitMenuItem.addActionListener(e -> Platform.runLater(() -> {
      Platform.exit();
      System.exit(0);
    }));

    return exitMenuItem;
  }

  private Image getIcon(String name) {
    return java.awt.Toolkit.getDefaultToolkit()
        .getImage(getClass().getResource(String.format(Locale.ROOT, "/images/icons/%s.png", name)));
  }

  private javafx.scene.image.Image getFxIcon(String name) {
    return new javafx.scene.image.Image(
        getClass().getResourceAsStream(String.format(Locale.ROOT, "/images/icons/%s.png", name)));
  }
}
