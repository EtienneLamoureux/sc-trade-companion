package tools.sctrade.companion.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
    setHgap(16);
    setVgap(6);

    ColumnConstraints col0 = new ColumnConstraints();
    col0.setPercentWidth(50);
    col0.setHgrow(Priority.ALWAYS);
    col0.setHalignment(HPos.RIGHT);

    ColumnConstraints col1 = new ColumnConstraints();
    col1.setPercentWidth(50);
    col1.setHgrow(Priority.ALWAYS);
    col1.setHalignment(HPos.LEFT);

    getColumnConstraints().addAll(col0, col1);
    addVerticalSpacer("settingsTopSpacer", rowIndex.getAndIncrement());

    buildUsernameField(userService);
    buildPrintscreenCommodityKeybindField(settings);
    buildPrintscreenItemKeybindField(settings);
    buildStarCitizenLivePathField(gameLogService);
    buildStarCitizenMonitorComboBox(settings);
    buildTextRow(LocalizationUtil.get("labelMyData"),
        settings.get(Setting.MY_DATA_PATH).toString());
    buildTextRow(LocalizationUtil.get("labelMyImages"),
        settings.get(Setting.MY_IMAGES_PATH).toString());
    addVerticalSpacer("settingsBottomSpacer", rowIndex.getAndIncrement());
  }

  private void buildUsernameField(UserService userService) {
    var usernameLabel = buildLabelWithHelp(rowIndex.get(), LocalizationUtil.get("labelUsername"),
        LocalizationUtil.get("tooltipUsername"));
    var usernameField = buildTextField(rowIndex.getAndIncrement(), userService.get().label());
    usernameField.setPromptText(LocalizationUtil.get("textFieldUsernamePlaceholder"));
    usernameLabel.setLabelFor(usernameField);
    usernameField.textProperty()
        .addListener((observable, oldValue, newValue) -> userService.updateUsername(newValue));
  }

  private void buildStarCitizenLivePathField(GameLogPathSubject gameLogService) {
    var starCitizenLivePathLabel =
        buildLabelWithHelp(rowIndex.get(), LocalizationUtil.get("labelStarCitizenLivePath"),
            LocalizationUtil.get("tooltipStarCitizenLivePath"));
    var starCitizenLivePathField = buildTextField(rowIndex.getAndIncrement(),
        gameLogService.getStarCitizenLivePath().orElse(null));
    starCitizenLivePathField
        .setPromptText(LocalizationUtil.get("textFieldStarCitizenLivePathPlaceholder"));
    starCitizenLivePathLabel.setLabelFor(starCitizenLivePathField);
    starCitizenLivePathField.textProperty().addListener(
        (observable, oldValue, newValue) -> gameLogService.setStarCitizenLivePath(newValue));
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
    var keybindLabel = buildLabelWithHelp(rowIndex.get(), keybindLabelText, tooltipText);

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

  private Label buildLabelWithHelp(int y, String text, String helpText) {
    Label label = new Label(text);
    Label helpIcon = new Label("\u24d8");
    helpIcon.getStyleClass().add("settings-help-icon");
    helpIcon.setTooltip(new Tooltip(helpText));

    HBox labelBox = new HBox(4, label, helpIcon);
    labelBox.setAlignment(Pos.CENTER_RIGHT);
    add(labelBox, 0, y);
    GridPane.setHalignment(labelBox, HPos.RIGHT);
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

  private String formatKeybind(int keyCode) {
    if (keyCode >= NativeKeyEvent.VC_F1 && keyCode <= NativeKeyEvent.VC_F24) {
      return String.format("F%d", keyCode - NativeKeyEvent.VC_F1 + 1);
    }

    return NativeKeyEvent.getKeyText(keyCode);
  }

  private void addVerticalSpacer(String id, int row) {
    Region spacer = new Region();
    spacer.setId(id);
    spacer.setMaxHeight(Double.MAX_VALUE);
    add(spacer, 0, row, 2, 1);
    GridPane.setVgrow(spacer, Priority.ALWAYS);
  }
}
