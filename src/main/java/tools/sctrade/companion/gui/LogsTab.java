package tools.sctrade.companion.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
  private final ObservableList<LogEntry> logs = FXCollections.observableArrayList();
  private final TableView<LogEntry> table = new TableView<>(logs);

  /**
   * Creates a new instance of the logs tab.
   */
  public LogsTab() {
    table.getStyleClass().add("logs-table");
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

  /**
   * Returns the number of log entries currently shown.
   *
   * @return the number of visible log entries
   */
  public int getLogCount() {
    return logs.size();
  }

  private void buildTable() {
    TableColumn<LogEntry, String> timeColumn =
        new TableColumn<>(LocalizationUtil.get("tableColumnTime"));
    timeColumn.setCellValueFactory(cell -> cell.getValue().timeProperty());
    timeColumn.setPrefWidth(150);
    timeColumn.setSortType(TableColumn.SortType.DESCENDING);

    TableColumn<LogEntry, String> typeColumn =
        new TableColumn<>(LocalizationUtil.get("tableColumnType"));
    typeColumn.setCellValueFactory(cell -> cell.getValue().typeProperty());
    typeColumn.setPrefWidth(50);

    TableColumn<LogEntry, String> messageColumn =
        new TableColumn<>(LocalizationUtil.get("tableColumnMessage"));
    messageColumn.setCellValueFactory(cell -> cell.getValue().messageProperty());

    table.getColumns().setAll(java.util.List.of(timeColumn, typeColumn, messageColumn));
    table.getSortOrder().setAll(java.util.List.of(timeColumn));
    table.sort();
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
