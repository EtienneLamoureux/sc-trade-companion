package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UsageTabTest {
  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenUsageTabWhenInitializedThenShowInstructionsInWebView() {
    UsageTab usageTab = JavaFxTestUtil.supplyOnFxThreadAndWait(UsageTab::new);

    ScrollPane scrollPane = assertInstanceOf(ScrollPane.class, usageTab.getCenter());
    assertInstanceOf(WebView.class, scrollPane.getContent());
  }
}
