package tools.sctrade.companion.swing;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.Arrays;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import tools.sctrade.companion.utils.LocalizationUtil;

public class CompanionGui extends JFrame {
  private static final long serialVersionUID = -983766141308946535L;

  private final String version;

  private JMenuBar menuBar;
  private JTabbedPane tabbedPane;

  public CompanionGui(String version) {
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
    menuBar = new JMenuBar();
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
    tabbedPane = new JTabbedPane();
    tabbedPane.addTab(LocalizationUtil.get("tabUsage"), new UsageTab());
    tabbedPane.addTab(LocalizationUtil.get("tabSettings"), new SettingsTab());
    tabbedPane.addTab(LocalizationUtil.get("tabLogs"), new LogsTab());

    add(tabbedPane);
  }

  private void setupTray() throws AWTException {
    if (SystemTray.isSupported()) {
      setDefaultCloseOperation(HIDE_ON_CLOSE);

      PopupMenu popupMenu = new PopupMenu();
      popupMenu.add(buildOpenMenuItem());
      popupMenu.add(buildExitMenuItem());

      TrayIcon trayIcon = new TrayIcon(getIcon("icon16"));
      trayIcon.setPopupMenu(popupMenu);
      trayIcon.setImageAutoSize(true);
      trayIcon.setToolTip(LocalizationUtil.get("applicationTitle"));

      SystemTray systemTray = SystemTray.getSystemTray();
      systemTray.add(trayIcon);
    }

    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  private MenuItem buildOpenMenuItem() {
    MenuItem openMenuItem = new MenuItem(LocalizationUtil.get("menuItemOpen"));
    openMenuItem.addActionListener(e -> setVisible(true));

    return openMenuItem;
  }

  private MenuItem buildExitMenuItem() {
    MenuItem exitMenuItem = new MenuItem(LocalizationUtil.get("menuItemExit"));
    exitMenuItem.addActionListener(e -> System.exit(0));

    return exitMenuItem;
  }

  private Image getIcon(String name) {
    return getToolkit()
        .getImage(getClass().getResource(String.format(Locale.ROOT, "/images/icons/%s.png", name)));
  }
}
