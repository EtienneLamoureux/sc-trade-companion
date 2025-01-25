package tools.sctrade.companion.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
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
    super();

    JLabel instructions = new JLabel(LocalizationUtil.get("instructions"));
    add(instructions);
  }
}
