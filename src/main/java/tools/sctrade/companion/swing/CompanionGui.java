package tools.sctrade.companion.swing;

import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Locale;
import javax.swing.JFrame;
import tools.sctrade.companion.utils.LocalizationUtil;

public class CompanionGui extends JFrame {
  private static final long serialVersionUID = -983766141308946535L;

  private final String version;

  public CompanionGui(String version) {
    this.version = version;
  }

  public void initialize() throws AWTException {
    setLookAndFeel();
    setIconImages();

    setTitle(
        String.format(Locale.ROOT, "%s %s", LocalizationUtil.get("applicationTitle"), version));
    setSize(300, 200);
    setLocationRelativeTo(null);

    setupTray();
  }

  private void setLookAndFeel() {
    FlatArcDarkOrangeIJTheme.setup();
  }

  private void setIconImages() {
    var iconPaths = Arrays.asList("icon128", "icon64", "icon32", "icon16");
    var iconImages = iconPaths.parallelStream().map(this::getIcon).toList();

    setIconImages(iconImages);
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
    } else {
      setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
  }

  private MenuItem buildOpenMenuItem() {
    MenuItem openMenuItem = new MenuItem(LocalizationUtil.get("trayMenuItemOpen"));
    openMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(true);
      }
    });

    return openMenuItem;
  }

  private MenuItem buildExitMenuItem() {
    MenuItem exitMenuItem = new MenuItem(LocalizationUtil.get("trayMenuItemExit"));
    exitMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });

    return exitMenuItem;
  }

  private Image getIcon(String name) {
    return getToolkit()
        .getImage(getClass().getResource(String.format(Locale.ROOT, "/images/icons/%s.png", name)));
  }
}
