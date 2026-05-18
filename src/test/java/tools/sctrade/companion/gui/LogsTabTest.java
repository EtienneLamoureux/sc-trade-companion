package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LogsTabTest {
  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenActiveSortWhenAddingLogThenNewRowRespectsSortOrder() {
    TestableLogsTab logsTab = JavaFxTestUtil.supplyOnFxThreadAndWait(TestableLogsTab::new);

    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      logsTab.addLog(new Object[] {"10:00", "WARN", "Zulu"});
      logsTab.addLog(new Object[] {"09:00", "INFO", "Alpha"});
      logsTab.sortByMessageAscending();
      logsTab.addLog(new Object[] {"08:00", "INFO", "Aaron"});
    });

    String firstMessage = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> logsTab.getMessageAt(0));
    assertEquals("Aaron", firstMessage);
  }

  @Test
  void givenLogRowWhenAddedThenShowItInTable() {
    TestableLogsTab logsTab = JavaFxTestUtil.supplyOnFxThreadAndWait(TestableLogsTab::new);

    JavaFxTestUtil
        .runOnFxThreadAndWait(() -> logsTab.addLog(new Object[] {"10:00", "INFO", "Ready"}));

    int itemCount = JavaFxTestUtil.supplyOnFxThreadAndWait(logsTab::getLogCount);
    assertEquals(1, itemCount);
  }

  @Test
  void givenLogsTableWhenBuiltThenExpandMessageColumnWhenResizedWider() {
    TestableLogsTab logsTab = JavaFxTestUtil.supplyOnFxThreadAndWait(TestableLogsTab::new);

    JavaFxTestUtil.runOnFxThreadAndWait(() -> logsTab.renderAtWidth(300));
    double initialMessageWidth =
        JavaFxTestUtil.supplyOnFxThreadAndWait(logsTab::getMessageColumnWidth);
    JavaFxTestUtil.runOnFxThreadAndWait(() -> logsTab.resizeToWidth(600));
    double resizedMessageWidth =
        JavaFxTestUtil.supplyOnFxThreadAndWait(logsTab::getMessageColumnWidth);

    assertTrue(resizedMessageWidth > initialMessageWidth,
        () -> String.format("initial=%s resized=%s", initialMessageWidth, resizedMessageWidth));
  }

  @Test
  void givenLogsTableWhenBuiltThenKeepTimeColumnAtFixedWidth() {
    TestableLogsTab logsTab = JavaFxTestUtil.supplyOnFxThreadAndWait(TestableLogsTab::new);

    JavaFxTestUtil.runOnFxThreadAndWait(() -> logsTab.renderAtWidth(300));
    double initialTimeWidth = JavaFxTestUtil.supplyOnFxThreadAndWait(logsTab::getTimeColumnWidth);
    JavaFxTestUtil.runOnFxThreadAndWait(() -> logsTab.resizeToWidth(600));
    double resizedTimeWidth = JavaFxTestUtil.supplyOnFxThreadAndWait(logsTab::getTimeColumnWidth);

    assertEquals(initialTimeWidth, resizedTimeWidth);
  }

  @Test
  void givenLogsTableWhenBuiltThenCapTimeColumnMaxWidth() {
    LogsTab logsTab = JavaFxTestUtil.supplyOnFxThreadAndWait(LogsTab::new);

    TableColumn<?, ?> timeColumn =
        JavaFxTestUtil.supplyOnFxThreadAndWait(() -> table(logsTab).getColumns().get(0));
    assertEquals(LogsTab.TIME_COLUMN_WIDTH, timeColumn.getMaxWidth());
  }

  private static TableView<?> table(LogsTab logsTab) {
    return (TableView<?>) logsTab.getCenter();
  }
}
