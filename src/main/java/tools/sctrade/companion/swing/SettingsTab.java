package tools.sctrade.companion.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import tools.sctrade.companion.utils.LocalizationUtil;

public class SettingsTab extends JPanel {
  private static final long serialVersionUID = -3532718267415423680L;

  public SettingsTab() {
    super();
    setLayout(new GridBagLayout());

    var usernameLabel = buildLabel(0, LocalizationUtil.get("labelUsername"));
    var usernameField = buildTextField(0, null);
    usernameField.putClientProperty("JTextField.placeholderText",
        LocalizationUtil.get("textFieldUsernamePlaceholder"));
    usernameLabel.setLabelFor(usernameField);

    var csvPathLabel = buildLabel(1, LocalizationUtil.get("labelMyData"));
    var csvPathField = buildTextField(1, null);
    csvPathField.setEditable(false);
    csvPathLabel.setLabelFor(csvPathField);

    var imagesPathLabel = buildLabel(2, LocalizationUtil.get("labelMyImages"));
    var imagesPathField = buildTextField(2, null);
    imagesPathField.setEditable(false);
    imagesPathLabel.setLabelFor(imagesPathField);
  }

  public JLabel buildLabel(int y, String string) {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.anchor = GridBagConstraints.EAST;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = y;

    JLabel label = new JLabel(string);

    add(label, gridBagConstraints);

    return label;
  }

  public JTextField buildTextField(int y, String value) {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.ipadx = 10;
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = y;

    JTextField textField = new JTextField(value);
    textField.setColumns(20);

    add(textField, gridBagConstraints);

    return textField;
  }
}
