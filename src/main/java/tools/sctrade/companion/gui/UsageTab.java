package tools.sctrade.companion.gui;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * The usage tab for the companion GUI. This is where users can see the instructions on how to use
 * this app.
 */
public class UsageTab extends BorderPane {
  /**
   * Creates a new instance of the usage tab.
   */
  public UsageTab() {
    WebView instructions = new WebView();
    instructions.getEngine()
        .loadContent(CompanionTheme.wrapInstructionsHtml(LocalizationUtil.get("instructions")));

    ScrollPane scrollPane = new ScrollPane(instructions);
    scrollPane.getStyleClass().add("usage-scroll-pane");
    scrollPane.setFitToHeight(true);
    scrollPane.setFitToWidth(true);
    setCenter(scrollPane);
  }
}
