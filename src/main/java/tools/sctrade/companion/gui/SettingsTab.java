package tools.sctrade.companion.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import tools.sctrade.companion.domain.gamelog.GameLogPathSubject;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.utils.GraphicsDeviceUtil;
import tools.sctrade.companion.utils.IncrementingInt;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * The settings tab for the companion GUI. This is where users can configure the application.
 *
 * @see SettingRepository
 */
public class SettingsTab extends JPanel {
  private static final long serialVersionUID = -3532718267415423680L;

  private IncrementingInt rowIndex;

  /**
   * Creates a new instance of the settings tab.
   *
   * @param userService User service.
   * @param gameLogService Game log service.
   * @param settings Settings repository.
   */
  public SettingsTab(UserService userService, GameLogPathSubject gameLogService,
      SettingRepository settings) {
    super();
    rowIndex = new IncrementingInt();

    setLayout(new GridBagLayout());

    buildUsernameField(userService);
    buildPrintscreenKeybindField(settings);
    buildStarCitizenLivePathField(gameLogService);
    buildStarCitizenMonitorComboBox(settings);
    buildTextRow(LocalizationUtil.get("labelMyData"),
        settings.get(Setting.MY_DATA_PATH).toString());
    buildTextRow(LocalizationUtil.get("labelMyImages"),
        settings.get(Setting.MY_IMAGES_PATH).toString());
  }

  private void buildUsernameField(UserService userService) {
    var usernameLabel = buildLabel(rowIndex.get(), LocalizationUtil.get("labelUsername"));
    var usernameField = buildTextField(rowIndex.getAndIncrement(), userService.get().label());
    usernameField.putClientProperty("JTextField.placeholderText",
        LocalizationUtil.get("textFieldUsernamePlaceholder"));
    usernameLabel.setLabelFor(usernameField);

    usernameField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        updateUsername();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateUsername();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateUsername();
      }

      private void updateUsername() {
        userService.updateUsername(usernameField.getText());
      }
    });

    var tooltip = buildLabel(rowIndex.getAndIncrement(), LocalizationUtil.get("tooltipUsername"));
    tooltip.putClientProperty("FlatLaf.styleClass", "small");
    tooltip.setEnabled(false);
    buildLabel(rowIndex.getAndIncrement(), " ");
  }

  private void buildStarCitizenLivePathField(GameLogPathSubject gameLogService) {
    var starCitizenLivePathLabel =
        buildLabel(rowIndex.get(), LocalizationUtil.get("labelStarCitizenLivePath"));
    var starCitizenLivePathField = buildTextField(rowIndex.getAndIncrement(),
        gameLogService.getStarCitizenLivePath().orElse(null));
    starCitizenLivePathField.putClientProperty("JTextField.placeholderText",
        LocalizationUtil.get("textFieldStarCitizenLivePathPlaceholder"));
    starCitizenLivePathLabel.setLabelFor(starCitizenLivePathField);

    starCitizenLivePathField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        updateStarCitizenLivePath();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateStarCitizenLivePath();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateStarCitizenLivePath();
      }

      private void updateStarCitizenLivePath() {
        gameLogService.setStarCitizenLivePath(starCitizenLivePathField.getText());
      }
    });

    var tooltip =
        buildLabel(rowIndex.getAndIncrement(), LocalizationUtil.get("tooltipStarCitizenLivePath"));
    tooltip.putClientProperty("FlatLaf.styleClass", "small");
    tooltip.setEnabled(false);
    buildLabel(rowIndex.getAndIncrement(), " ");
  }

  private void buildStarCitizenMonitorComboBox(SettingRepository settings) {
    var monitorIds = GraphicsDeviceUtil.getIds();
    String selectedMonitorId =
        settings.get(Setting.STAR_CITIZEN_MONITOR, GraphicsDeviceUtil.getPrimaryId());

    var starCitizenMonitorLabel =
        buildLabel(rowIndex.get(), LocalizationUtil.get("labelStarCitizenMonitor"));
    var starCitizenMonitorComboBox =
        buildComboBox(rowIndex.getAndIncrement(), monitorIds, selectedMonitorId);
    starCitizenMonitorLabel.setLabelFor(starCitizenMonitorComboBox);

    starCitizenMonitorComboBox.addActionListener(e -> {
      @SuppressWarnings("unchecked")
      var comboBox = (JComboBox<String>) e.getSource();
      String value = (String) comboBox.getSelectedItem();
      settings.set(Setting.STAR_CITIZEN_MONITOR, value);
    });
  }

  private void buildPrintscreenKeybindField(SettingRepository settings) {
    var keybindLabel = buildLabel(rowIndex.get(), LocalizationUtil.get("labelPrintscreenKeybind"));

    // Create a panel to hold both the display field and the button
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = rowIndex.getAndIncrement();
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;

    JPanel keybindPanel = new JPanel();
    keybindPanel.setLayout(new GridBagLayout());

    // Display field
    GridBagConstraints displayConstraints = new GridBagConstraints();
    displayConstraints.gridx = 0;
    displayConstraints.gridy = 0;
    displayConstraints.weightx = 1.0;
    displayConstraints.fill = GridBagConstraints.HORIZONTAL;
    displayConstraints.ipadx = 10;

    String currentKeybind =
        NativeKeyEvent.getKeyText(settings.get(Setting.PRINTSCREEN_KEYBIND, NativeKeyEvent.VC_F3));
    JTextField keybindField = new JTextField(currentKeybind);
    keybindField.setColumns(10);
    keybindField.setEditable(false);
    keybindPanel.add(keybindField, displayConstraints);

    // Button to capture keybind
    GridBagConstraints buttonConstraints = new GridBagConstraints();
    buttonConstraints.gridx = 1;
    buttonConstraints.gridy = 0;
    buttonConstraints.insets = new java.awt.Insets(0, 5, 0, 0);

    JButton captureButton = new JButton(LocalizationUtil.get("buttonCaptureKeybind"));
    keybindPanel.add(captureButton, buttonConstraints);

    add(keybindPanel, gridBagConstraints);
    keybindLabel.setLabelFor(keybindPanel);

    // Add action listener for the capture button
    captureButton.addActionListener(e -> {
      // Disable button to prevent multiple clicks while listening
      captureButton.setText(LocalizationUtil.get("buttonListeningForKey"));
      captureButton.setEnabled(false);

      keybindField.requestFocusInWindow();

      // Create a temporary key listener to capture the next key press
      NativeKeyListener captureListener = new NativeKeyListener() {
        @Override
        public void nativeKeyPressed(NativeKeyEvent event) {
          int keyCode = event.getKeyCode();

          // Update UI on the Event Dispatch Thread for valid keys
          javax.swing.SwingUtilities.invokeLater(() -> {
            keybindField.setText(NativeKeyEvent.getKeyText(keyCode));
            settings.set(Setting.PRINTSCREEN_KEYBIND, keyCode);
          });

          // Cleanup - remove listener and reset button state regardless of key validity
          // Note: GlobalScreen.removeNativeKeyListener is thread-safe and can be called
          // from the native event thread
          try {
            GlobalScreen.removeNativeKeyListener(this);
          } finally {
            // Reset button state on the Event Dispatch Thread
            javax.swing.SwingUtilities.invokeLater(() -> {
              captureButton.setText(LocalizationUtil.get("buttonCaptureKeybind"));
              captureButton.setEnabled(true);
            });
          }
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent event) {
          // Not used
        }

        @Override
        public void nativeKeyTyped(NativeKeyEvent event) {
          // Not used
        }
      };

      GlobalScreen.addNativeKeyListener(captureListener);
    });

    var tooltip =
        buildLabel(rowIndex.getAndIncrement(), LocalizationUtil.get("tooltipPrintscreenKeybind"));
    tooltip.putClientProperty("FlatLaf.styleClass", "small");
    tooltip.setEnabled(false);
    buildLabel(rowIndex.getAndIncrement(), " ");
  }

  private void buildTextRow(String label, String value) {
    var jLabel = buildLabel(rowIndex.get(), label);
    var jTextField = buildTextField(rowIndex.getAndIncrement(), value);
    jTextField.setEditable(false);
    jLabel.setLabelFor(jTextField);
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

  private JComboBox<String> buildComboBox(int y, Collection<String> values, String value) {
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = y;

    Vector<String> orderedValues = new Vector<>(values);
    Collections.sort(orderedValues);
    JComboBox<String> comboBox = new JComboBox<>(orderedValues);
    comboBox.setSelectedItem(value);

    add(comboBox, gridBagConstraints);

    return comboBox;
  }
}
