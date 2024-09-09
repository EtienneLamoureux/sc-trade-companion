package tools.sctrade.companion.gui;

import java.awt.AWTException;
import java.awt.Image;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;

import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import tools.sctrade.companion.domain.notification.NotificationLevel;
import tools.sctrade.companion.domain.notification.NotificationRepository;
import tools.sctrade.companion.domain.user.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.utils.LocalizationUtil;
import tools.sctrade.companion.utils.TimeFormat;
import tools.sctrade.companion.utils.TimeUtil;

public class CompanionGui extends JFrame implements NotificationRepository {
  private static final long serialVersionUID = -983766141308946535L;

  private transient UserService userService;
  private transient SettingRepository settings;
  private final String version;
  private LogsTab logsTab;

  public CompanionGui(UserService userService, SettingRepository settings, String version) {
    this.userService = userService;
    this.settings = settings;
    this.version = version;
  }

  public void initialize() throws AWTException {
    setLookAndFeel();
    setIconImages();

    setTitle(
        String.format(Locale.ROOT, "%s %s", LocalizationUtil.get("applicationTitle"), version));

    setSize(600, 575);
    setLocationRelativeTo(null);

    buildMenuBar();
    buildTabs();
    setupTray();
  }

  @Override
  public void add(NotificationLevel level, String message) {
    if (logsTab != null) {
      logsTab.addLog(
          new Object[] {TimeUtil.getNowAsString(TimeFormat.CSV_COLUMN), level.toString(), message});
    }
  }

  private void setLookAndFeel() {
    FlatArcDarkOrangeIJTheme.setup();
    FlatLaf.updateUI();
  }

  private void setIconImages() {
    var iconPaths = Arrays.asList("icon128", "icon64", "icon32", "icon16");
    var iconImages = iconPaths.parallelStream().map(this::getIcon).toList();

    setIconImages(iconImages);
  }

  private void buildMenuBar() {
    var menuBar = new JMenuBar();
    menuBar.add(buildFileMenu());

    setJMenuBar(menuBar);
  }

  private JMenu buildFileMenu() {
    var fileMenu = new JMenu();
    fileMenu.setText(LocalizationUtil.get("menuFile"));

    JMenuItem closeMenuItem = new JMenuItem();
    closeMenuItem.setText(LocalizationUtil.get("menuItemSendToTray"));
    closeMenuItem.addActionListener(e -> setVisible(false));
    fileMenu.add(closeMenuItem);

    JMenuItem exitMenuItem = new JMenuItem();
    exitMenuItem.setText(LocalizationUtil.get("menuItemExit"));
    exitMenuItem.addActionListener(e -> System.exit(0));
    fileMenu.add(exitMenuItem);
    return fileMenu;
  }

  private void buildTabs() {
    logsTab = new LogsTab();

    var tabbedPane = new JTabbedPane();
    tabbedPane.addTab(LocalizationUtil.get("tabUsage"), new UsageTab());
    tabbedPane.addTab(LocalizationUtil.get("tabSettings"), new SettingsTab(userService, settings));
    tabbedPane.addTab(LocalizationUtil.get("tabLogs"), logsTab);

    add(tabbedPane);
  }

  private void setupTray() {  
    SystemTray systemTray = SystemTray.get(LocalizationUtil.get("applicationTitle"));

    if (systemTray == null) {
      setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    else{
      setDefaultCloseOperation(HIDE_ON_CLOSE);
      systemTray.setImage(getIcon("icon128"));
      
      systemTray.getMenu().add(new MenuItem(LocalizationUtil.get("menuItemOpen"), e->{ 
        setVisible(true); 
      }));

      systemTray.getMenu().add(new MenuItem(LocalizationUtil.get("menuItemExit"), e->{
        systemTray.shutdown();
        System.exit(0);
      }));
    }
  }

  private Image getIcon(String name) {
    return getToolkit()
        .getImage(getClass().getResource(String.format(Locale.ROOT, "/images/icons/%s.png", name)));
  }
}
