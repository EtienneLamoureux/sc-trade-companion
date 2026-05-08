package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LogsTabTest {
  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenLogRowWhenAddedThenShowItInTable() {
    LogsTab logsTab = JavaFxTestUtil.supplyOnFxThreadAndWait(LogsTab::new);

    JavaFxTestUtil
        .runOnFxThreadAndWait(() -> logsTab.addLog(new Object[] {"10:00", "INFO", "Ready"}));

    int itemCount = JavaFxTestUtil.supplyOnFxThreadAndWait(logsTab::getLogCount);
    assertEquals(1, itemCount);
  }
}
