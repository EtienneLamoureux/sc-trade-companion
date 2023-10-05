package tools.sctrade.companion;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
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

  public CompanionApplication() {
    initUI();
  }

  private void initUI() {
    var quitButton = new JButton("Quit");

    quitButton.addActionListener((ActionEvent event) -> {
      System.exit(0);
    });

    createLayout(quitButton);

    setTitle("Quit button");
    setSize(300, 200);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
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
    var ctx = new SpringApplicationBuilder(CompanionApplication.class).headless(false)
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
      var ex = ctx.getBean(CompanionApplication.class);
      ex.setVisible(true);
    });
  }
}
