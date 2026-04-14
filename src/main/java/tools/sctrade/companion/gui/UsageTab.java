package tools.sctrade.companion.gui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * The usage tab for the companion GUI. This is where users can see the instructions on how to use
 * this app.
 */
public class UsageTab extends JPanel {
  private static final long serialVersionUID = -5302283386634373931L;

  /**
   * Creates a new instance of the usage tab.
   */
  public UsageTab() {
    super(new BorderLayout());

    JEditorPane instructions = new JEditorPane("text/html", LocalizationUtil.get("instructions")) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean getScrollableTracksViewportWidth() {
        return true;
      }
    };
    instructions.setEditable(false);
    instructions.setOpaque(false);
    instructions.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    JScrollPane scrollPane = new JScrollPane(instructions);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    add(scrollPane, BorderLayout.CENTER);
  }
}
