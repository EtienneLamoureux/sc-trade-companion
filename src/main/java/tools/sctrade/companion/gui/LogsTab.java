package tools.sctrade.companion.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import tools.sctrade.companion.domain.notification.NotificationRepository;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * The logs tab for the companion GUI. This is where users can see the notifications.
 *
 * @see NotificationRepository
 */
public class LogsTab extends BorderPane {
  static final int TIME_COLUMN_WIDTH = 175;
  static final int LOG_TYPE_COLUMN_WIDTH = 75;

  private final ObservableList<LogEntry> logs = FXCollections.observableArrayList();
  private final TableView<LogEntry> table = new TableView<>();
  private final SortedList<LogEntry> sortedLogs = new SortedList<>(logs);

  /**
   * Creates a new instance of the logs tab.
   */
  public LogsTab() {
    table.getStyleClass().add("logs-table");
    sortedLogs.comparatorProperty().bind(table.comparatorProperty());
    table.setItems(sortedLogs);
    buildTable();
    setCenter(table);
  }

  /**
   * Adds a log to the table.
   *
   * @param row The row to add.
   */
  public void addLog(Object[] row) {
    logs.add(new LogEntry(String.valueOf(row[0]), String.valueOf(row[1]), String.valueOf(row[2])));
  }

  private void buildTable() {
    TableColumn<LogEntry, String> timeColumn =
        new TableColumn<>(LocalizationUtil.get("tableColumnTime"));
    timeColumn.setCellValueFactory(cell -> cell.getValue().timeProperty());
    timeColumn.setMinWidth(TIME_COLUMN_WIDTH);
    timeColumn.setPrefWidth(TIME_COLUMN_WIDTH);
    timeColumn.setMaxWidth(TIME_COLUMN_WIDTH);
    timeColumn.setSortType(TableColumn.SortType.DESCENDING);
    timeColumn.setReorderable(false);
    timeColumn.setResizable(false);

    TableColumn<LogEntry, String> typeColumn =
        new TableColumn<>(LocalizationUtil.get("tableColumnType"));
    typeColumn.setCellValueFactory(cell -> cell.getValue().typeProperty());
    typeColumn.setMinWidth(LOG_TYPE_COLUMN_WIDTH);
    typeColumn.setPrefWidth(LOG_TYPE_COLUMN_WIDTH);
    typeColumn.setMaxWidth(LOG_TYPE_COLUMN_WIDTH);
    typeColumn.setReorderable(false);
    typeColumn.setResizable(false);

    TableColumn<LogEntry, String> messageColumn =
        new TableColumn<>(LocalizationUtil.get("tableColumnMessage"));
    messageColumn.setCellValueFactory(cell -> cell.getValue().messageProperty());
    messageColumn.setMinWidth(0);
    messageColumn.setReorderable(false);

    table.getColumns().setAll(java.util.List.of(timeColumn, typeColumn, messageColumn));
    synchronizeMessageColumnWidth(messageColumn);
    table.getSortOrder().setAll(java.util.List.of(timeColumn));
    table.sort();
  }

  private void synchronizeMessageColumnWidth(TableColumn<LogEntry, String> messageColumn) {
    table.widthProperty()
        .addListener((observable, oldWidth, newWidth) -> updateMessageColumnWidth(messageColumn));
    updateMessageColumnWidth(messageColumn);
  }

  private void updateMessageColumnWidth(TableColumn<LogEntry, String> messageColumn) {
    messageColumn
        .setPrefWidth(Math.max(0, table.getWidth() - TIME_COLUMN_WIDTH - LOG_TYPE_COLUMN_WIDTH));
  }

  private static final class LogEntry {
    private final StringProperty time;
    private final StringProperty type;
    private final StringProperty message;

    private LogEntry(String time, String type, String message) {
      this.time = new SimpleStringProperty(time);
      this.type = new SimpleStringProperty(type);
      this.message = new SimpleStringProperty(message);
    }

    private StringProperty timeProperty() {
      return time;
    }

    private StringProperty typeProperty() {
      return type;
    }

    private StringProperty messageProperty() {
      return message;
    }
  }
}
