package tools.sctrade.companion;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class CompanionApplication extends JFrame {
  private static final long serialVersionUID = 1L;

  public CompanionApplication() throws IOException, AWTException {
    initUI();
  }

  private void initUI() throws IOException, AWTException {
    var quitButton = new JButton("Quit");

    quitButton.addActionListener((ActionEvent event) -> {
      System.exit(0);
    });

    createLayout(quitButton);

    var iconPaths = Arrays.asList("/images/icon128.png", "/images/icon64.png", "/images/icon32.png",
        "/images/icon16.png");
    var iconImages = iconPaths.parallelStream().map(n -> getClass().getResource(n))
        .map(n -> getToolkit().createImage(n)).toList();
    setIconImages(iconImages);
    setTitle("SC Trade Companion");
    setSize(300, 200);
    setLocationRelativeTo(null);
    if (SystemTray.isSupported()) {
      setDefaultCloseOperation(HIDE_ON_CLOSE);
      SystemTray systemTray = SystemTray.getSystemTray();
      TrayIcon trayIcon =
          new TrayIcon(getToolkit().getImage(getClass().getResource("/images/icon16.png")));
      PopupMenu popupMenu = new PopupMenu();
      MenuItem openMenuItem = new MenuItem("Open");
      openMenuItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          setVisible(true);
        }
      });

      MenuItem exitMenuItem = new MenuItem("Exit");
      exitMenuItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          System.exit(0);
        }
      });

      popupMenu.add(openMenuItem);
      popupMenu.add(exitMenuItem);
      trayIcon.setPopupMenu(popupMenu);
      systemTray.add(trayIcon);
    } else {
      setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
  }

  private void createLayout(JComponent... arg) {
    var pane = getContentPane();
    var gl = new GroupLayout(pane);
    pane.setLayout(gl);

    gl.setAutoCreateContainerGaps(true);

    gl.setHorizontalGroup(gl.createSequentialGroup().addComponent(arg[0]));

    gl.setVerticalGroup(gl.createSequentialGroup().addComponent(arg[0]));
  }

  public static void main(String[] args) {
    var context = new SpringApplicationBuilder(CompanionApplication.class).headless(false)
        .web(WebApplicationType.NONE).run(args);

    try {
      GlobalScreen.registerNativeHook();
    } catch (NativeHookException ex) {
      System.err.println("There was a problem registering the native hook.");
      System.err.println(ex.getMessage());

      System.exit(1);
    }

    GlobalScreen.addNativeKeyListener(new KeyListener());

    EventQueue.invokeLater(() -> {
      var ex = context.getBean(CompanionApplication.class);
      ex.setVisible(true);
    });
  }
}
