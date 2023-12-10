package tools.sctrade.companion.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import tools.sctrade.companion.domain.user.Setting;
import tools.sctrade.companion.domain.user.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.utils.LocalizationUtil;

public class SettingsTab extends JPanel {
  private static final long serialVersionUID = -3532718267415423680L;

  public SettingsTab(UserService userService, SettingRepository settings) {
    super();
    setLayout(new GridBagLayout());

    buildUsernameField(userService);
    buildDataPathField(settings.get(Setting.MY_DATA_PATH).toString());
    buildImagesPathField(settings.get(Setting.MY_IMAGES_PATH).toString());
  }

  private void buildUsernameField(UserService userService) {
    var usernameLabel = buildLabel(0, LocalizationUtil.get("labelUsername"));
    var usernameField = buildTextField(0, userService.get().label());
    usernameField.putClientProperty("JTextField.placeholderText",
        LocalizationUtil.get("textFieldUsernamePlaceholder"));
    usernameLabel.setLabelFor(usernameField);

    usernameField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        updateUserLabel();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateUserLabel();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateUserLabel();
      }

      private void updateUserLabel() {
        userService.updateUsername(usernameField.getText());
      }
    });
  }

  private void buildDataPathField(String dataPath) {
    var dataPathLabel = buildLabel(1, LocalizationUtil.get("labelMyData"));
    var dataPathField = buildTextField(1, dataPath);
    dataPathField.setEditable(false);
    dataPathLabel.setLabelFor(dataPathField);
  }

  private void buildImagesPathField(String imagesPath) {
    var imagesPathLabel = buildLabel(2, LocalizationUtil.get("labelMyImages"));
    var imagesPathField = buildTextField(2, imagesPath);
    imagesPathField.setEditable(false);
    imagesPathLabel.setLabelFor(imagesPathField);
  }

  private JLabel buildLabel(int y, String string) {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.anchor = GridBagConstraints.EAST;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = y;

    JLabel label = new JLabel(string);

    add(label, gridBagConstraints);

    return label;
  }

  private JTextField buildTextField(int y, String value) {
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
