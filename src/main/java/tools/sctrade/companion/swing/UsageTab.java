package tools.sctrade.companion.swing;

import javax.swing.JLabel;
import javax.swing.JPanel;
import tools.sctrade.companion.utils.LocalizationUtil;

public class UsageTab extends JPanel {
  private static final long serialVersionUID = -5302283386634373931L;

  public UsageTab() {
    super();

    JLabel instructions = new JLabel(LocalizationUtil.get("instructions"));
    add(instructions);
  }
}
