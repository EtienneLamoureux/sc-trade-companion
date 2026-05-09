package tools.sctrade.companion.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
public class SettingsTab extends GridPane {
  private final IncrementingInt rowIndex;

  /**
   * Creates a new instance of the settings tab.
   *
   * @param userService User service.
   * @param gameLogService Game log service.
   * @param settings Settings repository.
   */
  public SettingsTab(UserService userService, GameLogPathSubject gameLogService,
      SettingRepository settings) {
    rowIndex = new IncrementingInt();

    setPadding(new Insets(10));
    setHgap(10);
    setVgap(6);

    buildUsernameField(userService);
    buildPrintscreenCommodityKeybindField(settings);
    buildPrintscreenItemKeybindField(settings);
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
    usernameField.setPromptText(LocalizationUtil.get("textFieldUsernamePlaceholder"));
    usernameLabel.setLabelFor(usernameField);
    usernameField.textProperty()
        .addListener((observable, oldValue, newValue) -> userService.updateUsername(newValue));

    buildTooltip(LocalizationUtil.get("tooltipUsername"));
  }

  private void buildStarCitizenLivePathField(GameLogPathSubject gameLogService) {
    var starCitizenLivePathLabel =
        buildLabel(rowIndex.get(), LocalizationUtil.get("labelStarCitizenLivePath"));
    var starCitizenLivePathField = buildTextField(rowIndex.getAndIncrement(),
        gameLogService.getStarCitizenLivePath().orElse(null));
    starCitizenLivePathField
        .setPromptText(LocalizationUtil.get("textFieldStarCitizenLivePathPlaceholder"));
    starCitizenLivePathLabel.setLabelFor(starCitizenLivePathField);
    starCitizenLivePathField.textProperty().addListener(
        (observable, oldValue, newValue) -> gameLogService.setStarCitizenLivePath(newValue));

    buildTooltip(LocalizationUtil.get("tooltipStarCitizenLivePath"));
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
    starCitizenMonitorComboBox.valueProperty().addListener(
        (observable, oldValue, newValue) -> settings.set(Setting.STAR_CITIZEN_MONITOR, newValue));
  }

  private void buildPrintscreenCommodityKeybindField(SettingRepository settings) {
    buildPrintscreenKeybindField(settings, Setting.PRINTSCREEN_COMMODITY_KEYBIND,
        NativeKeyEvent.VC_F3, LocalizationUtil.get("labelPrintscreenCommodityKeybind"),
        "commodityKeybindField", LocalizationUtil.get("tooltipPrintscreenKeybind"));
  }

  private void buildPrintscreenItemKeybindField(SettingRepository settings) {
    buildPrintscreenKeybindField(settings, Setting.PRINTSCREEN_ITEM_KEYBIND, NativeKeyEvent.VC_F3,
        LocalizationUtil.get("labelPrintscreenItemKeybind"), "itemKeybindField",
        LocalizationUtil.get("tooltipPrintscreenItemKeybind"));
  }

  private void buildPrintscreenKeybindField(SettingRepository settings, Setting setting,
      int defaultKeybind, String keybindLabelText, String keybindFieldId, String tooltipText) {
    var keybindLabel = buildLabel(rowIndex.get(), keybindLabelText);

    String currentKeybind = formatKeybind(settings.get(setting, defaultKeybind));
    TextField keybindField = new TextField(currentKeybind);
    keybindField.setId(keybindFieldId);
    keybindField.setEditable(false);
    keybindField.setPrefColumnCount(10);
    HBox.setHgrow(keybindField, Priority.ALWAYS);

    Button captureButton = new Button(LocalizationUtil.get("buttonCaptureKeybind"));

    HBox keybindPanel = new HBox(5, keybindField, captureButton);
    add(keybindPanel, 1, rowIndex.getAndIncrement());
    keybindLabel.setLabelFor(keybindField);

    captureButton.setOnAction(event -> {
      captureButton.setText(LocalizationUtil.get("buttonListeningForKey"));
      captureButton.setDisable(true);
      keybindField.requestFocus();

      NativeKeyListener captureListener = new NativeKeyListener() {
        @Override
        public void nativeKeyPressed(NativeKeyEvent event) {
          int keyCode = event.getKeyCode();

          Platform.runLater(() -> {
            keybindField.setText(formatKeybind(keyCode));
            settings.set(setting, keyCode);
          });

          try {
            GlobalScreen.removeNativeKeyListener(this);
          } finally {
            Platform.runLater(() -> {
              captureButton.setText(LocalizationUtil.get("buttonCaptureKeybind"));
              captureButton.setDisable(false);
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

    buildTooltip(tooltipText);
  }

  private void buildTextRow(String label, String value) {
    var fxLabel = buildLabel(rowIndex.get(), label);
    var textField = buildTextField(rowIndex.getAndIncrement(), value);
    textField.setEditable(false);
    fxLabel.setLabelFor(textField);
  }

  private Label buildLabel(int y, String text) {
    Label label = new Label(text);
    add(label, 0, y);
    GridPane.setHalignment(label, HPos.RIGHT);
    return label;
  }

  private TextField buildTextField(int y, String value) {
    TextField textField = new TextField(value == null ? "" : value);
    textField.setPrefColumnCount(20);
    add(textField, 1, y);
    return textField;
  }

  private ComboBox<String> buildComboBox(int y, Collection<String> values, String value) {
    var orderedValues = new ArrayList<>(values);
    Collections.sort(orderedValues);

    ComboBox<String> comboBox = new ComboBox<>();
    comboBox.getItems().setAll(orderedValues);
    comboBox.setValue(value);
    add(comboBox, 1, y);
    return comboBox;
  }

  private void buildTooltip(String text) {
    Label tooltip = buildLabel(rowIndex.getAndIncrement(), text);
    tooltip.setDisable(true);
    buildLabel(rowIndex.getAndIncrement(), " ");
  }

  private String formatKeybind(int keyCode) {
    if (keyCode >= NativeKeyEvent.VC_F1 && keyCode <= NativeKeyEvent.VC_F24) {
      return String.format("F%d", keyCode - NativeKeyEvent.VC_F1 + 1);
    }

    return NativeKeyEvent.getKeyText(keyCode);
  }
}
