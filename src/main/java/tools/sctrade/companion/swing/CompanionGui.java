package tools.sctrade.companion.swing;

import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.Locale;
import javax.swing.*;

import tools.sctrade.companion.utils.LocalizationUtil;

public class CompanionGui extends JFrame {
  private static final long serialVersionUID = -983766141308946535L;
  public static final int StartWidth = 450;
  public static final int StartHeight = 150;
  private JLabel SCTradeToolsIcon;
  private JLabel UserNameHint;
  private JTextField UsernameBox;
  private JCheckBox UsernameCheckBox;
  private JTextField UsernameCheckBoxHint;

  private final String version;

  public CompanionGui(String version) {
    this.version = version;
  }

  public void initialize() throws AWTException {
    setLookAndFeel();
    setIconImages();

    // Set UI Scale on boot.
    InitializeUI();
    BuildUI();

    // Rebuild UI on rescale.
      // NOTE: Use getRootPanel() to ensure that, if we're nested as a result of some weird event, we can still rescale.
    this.getRootPane().addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        // This is called whenever the window is resized.
        BuildUI();
      }
    });

    setupTray();
  }

  // On first boot, initialize all elements on screen.
  private void InitializeUI() {
    // Set initial size and title.
    setSize(450, 150);
    setTitle(LocalizationUtil.get("applicationTitle"));

    // Create components for BuildUI() to move.
      // Below region is uncommented, since the code is very self-explanatory. Message me if you need help.
    UserNameHint = new JLabel(LocalizationUtil.get("usernameHint"));
    UserNameHint.setVisible(true);
    UserNameHint.setForeground(Color.black);
    add(UserNameHint);

    SCTradeToolsIcon = new JLabel(new ImageIcon(getIcon("icon64")));
    SCTradeToolsIcon.setVisible(true);
    add(SCTradeToolsIcon);

    UsernameBox = new JTextField(LocalizationUtil.get("usernameBoxPrefill"));
    UsernameBox.setVisible(true);
    add(UsernameBox);

    UsernameCheckBox = new JCheckBox();
    UsernameCheckBox.setVisible(true);
    add(UsernameCheckBox);

    UsernameCheckBoxHint = new JTextField(LocalizationUtil.get("usernameCheckBoxHint"));
    UsernameCheckBoxHint.setVisible(true);

      // There's probably a better way to do this, but this was just what came to mind first.
    UsernameCheckBoxHint.setEditable(false);
    UsernameCheckBoxHint.setOpaque(false);
    UsernameCheckBoxHint.setBorder(null);
    UsernameCheckBoxHint.setForeground(Color.black);
    add(UsernameCheckBoxHint);

    // Move to center.
    setLocationRelativeTo(null);
  }

  // All this method does is move stuff! Don't initialize stuff here!
  private void BuildUI() {
    // There's probably a better way to do this, but I like to parametrize my UIs, so I have stuff update on window resize.
      // My method is to define things in terms of their percentages of the window's height/width.
    double WidthScalar = 0.01f * this.getRootPane().getWidth(), HeightScalar = 0.01f * this.getRootPane().getHeight();
    setLayout(null);

    // Add icon to top left corner.
    // System.out.println("WidthScalar: " + WidthScalar + "HeightScalar: " + HeightScalar);
    int IconSides = 64;
    // TODO: Handle edge cases where the IconSides < icon64 sides.
      // Something like this for dimensions: (int) Math.min(64 * WidthScalar, 64 * HeightScalar);
      // Needs a method for rescaling the image, though. All this does is change the border box.

    // Place the icon in the top left corner, using a 5% gap on the sides.
    SCTradeToolsIcon.setBounds(
            (int) (95.0 * WidthScalar - IconSides), // Note, subtract IconSides length here such that the icon leaves that 5% gap correctly.
            (int) (5.0 * HeightScalar),
            IconSides,
            IconSides
    );

    // Add text, place it with a 5% gap from the top right, with 85% width, and a constant 24px height.
    UserNameHint.setBounds((int) (5.0 * WidthScalar), (int) (5.0 * HeightScalar), (int) (85.0 * WidthScalar), 24);

    // Place the username box 5% below the UserNameHint component, also with constant 24px height.
    UsernameBox.setBounds(
            (int) (5.0 * WidthScalar),
            (int) (10.0 * HeightScalar) + 24,
            (int) (85.0 * WidthScalar),
            24
    );

    // Place the checkbox 5% below all that above. (15% at this point.)
    UsernameCheckBox.setBounds((int) (5.0 * WidthScalar), (int) (15.0 * HeightScalar) + 48, 24, 24);

    // Also put its label next to it.
    UsernameCheckBoxHint.setBounds((int) (5.0 * WidthScalar) + 30, (int) (15.0 * HeightScalar) + 48, (int) (85.0 * WidthScalar) - 30, 24);
  }

  public String GetUsername() {
    return UsernameBox.getText();
  }

  public boolean GetUsernameEnabled() {
    return UsernameCheckBox.isSelected();
  }

  // On first boot, initialize all elements on screen.
  private void InitializeUI() {
    // Set initial size and title.
    setSize(StartWidth, StartHeight);

    // Create components for BuildUI() to move.
      // Below region is uncommented, since the code is very self-explanatory. Message me if you need help.
    UserNameHint = new JLabel(LocalizationUtil.get("usernameHint"));
    UserNameHint.setVisible(true);
    UserNameHint.setForeground(Color.black);
    add(UserNameHint);

    SCTradeToolsIcon = new JLabel(new ImageIcon(getIcon("icon64")));
    SCTradeToolsIcon.setVisible(true);
    add(SCTradeToolsIcon);

    UsernameBox = new JTextField(LocalizationUtil.get("usernameBoxPrefill"));
    UsernameBox.setVisible(true);
    add(UsernameBox);

    UsernameCheckBox = new JCheckBox();
    UsernameCheckBox.setVisible(true);
    add(UsernameCheckBox);

    UsernameCheckBoxHint = new JTextField(LocalizationUtil.get("usernameCheckBoxHint"));
    UsernameCheckBoxHint.setVisible(true);

      // There's probably a better way to do this, but this was just what came to mind first.
    UsernameCheckBoxHint.setEditable(false);
    UsernameCheckBoxHint.setOpaque(false);
    UsernameCheckBoxHint.setBorder(null);
    UsernameCheckBoxHint.setForeground(Color.black);
    add(UsernameCheckBoxHint);

    // Show version in Frame title.
    setTitle(String.format(Locale.ROOT, "%s %s", LocalizationUtil.get("applicationTitle"), version));
    setLocationRelativeTo(null);
  }

  // All this method does is move stuff! Don't initialize stuff here!
  private void BuildUI() {
    // There's probably a better way to do this, but I like to parametrize my UIs, so I have stuff update on window resize.
      // My method is to define things in terms of their percentages of the window's height/width.
    double WidthScalar = 0.01f * this.getRootPane().getWidth(), HeightScalar = 0.01f * this.getRootPane().getHeight();
    setLayout(null);

    // First things first, if the window has been resized to below 300*200, hide the icon.
    if (getWidth() < StartWidth || getHeight() < StartHeight) {
      SCTradeToolsIcon.setVisible(false);
    } else SCTradeToolsIcon.setVisible(true);

    // Add icon to top left corner.
    // System.out.println("WidthScalar: " + WidthScalar + "HeightScalar: " + HeightScalar);
    int IconSides = 64;

    // Place the icon in the top left corner, using a 5% gap on the sides.
    SCTradeToolsIcon.setBounds(
            (int) (95.0 * WidthScalar - IconSides), // Note, subtract IconSides length here such that the icon leaves that 5% gap correctly.
            (int) (5.0 * HeightScalar),
            IconSides,
            IconSides
    );

    // Add text, place it with a 5% gap from the top right, with 85% width, and a constant 24px height.
    UserNameHint.setBounds((int) (5.0 * WidthScalar), (int) (5.0 * HeightScalar), (int) (85.0 * WidthScalar), 24);

    // Place the username box 5% below the UserNameHint component, also with constant 24px height.
    UsernameBox.setBounds(
            (int) (5.0 * WidthScalar),
            (int) (10.0 * HeightScalar) + 24,
            (int) (85.0 * WidthScalar),
            24
    );

    // Place the checkbox 5% below all that above. (15% at this point.)
    UsernameCheckBox.setBounds((int) (5.0 * WidthScalar), (int) (15.0 * HeightScalar) + 48, 24, 24);

    // Also put its label next to it.
    UsernameCheckBoxHint.setBounds((int) (5.0 * WidthScalar) + 30, (int) (15.0 * HeightScalar) + 48, (int) (85.0 * WidthScalar) - 30, 24);
  }

  public String GetUsername() {
    return UsernameBox.getText();
  }

  public boolean GetUsernameEnabled() {
    return UsernameCheckBox.isSelected();
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
