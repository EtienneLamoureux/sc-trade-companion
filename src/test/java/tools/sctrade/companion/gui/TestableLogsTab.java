package tools.sctrade.companion.gui;

import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

class TestableLogsTab extends LogsTab {
  private static final double TEST_HEIGHT = 300;

  private Stage stage;

  int getLogCount() {
    return table().getItems().size();
  }

  String getMessageAt(int index) {
    return String.valueOf(messageColumn().getCellObservableValue(index).getValue());
  }

  double getTimeColumnWidth() {
    return timeColumn().getWidth();
  }

  double getMessageColumnWidth() {
    return messageColumn().getWidth();
  }

  void renderAtWidth(double width) {
    stage = new Stage();
    stage.setScene(new Scene(this, width, TEST_HEIGHT));
    stage.show();
    resizeToWidth(width);
  }

  void resizeToWidth(double width) {
    stage.setWidth(width);
    stage.setHeight(TEST_HEIGHT);
    resize(width, TEST_HEIGHT);
    table().resize(width, TEST_HEIGHT);
    stage.getScene().getRoot().applyCss();
    stage.getScene().getRoot().layout();
    table().layout();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  void sortByMessageAscending() {
    TableColumn messageColumn = messageColumn();
    messageColumn.setSortType(TableColumn.SortType.ASCENDING);
    table().getSortOrder().setAll(messageColumn);
    table().sort();
  }

  @SuppressWarnings("unchecked")
  private TableColumn<?, String> messageColumn() {
    return (TableColumn<?, String>) table().getColumns().get(2);
  }

  private TableColumn<?, ?> timeColumn() {
    return table().getColumns().get(0);
  }

  private TableView<?> table() {
    return (TableView<?>) getCenter();
  }
}
