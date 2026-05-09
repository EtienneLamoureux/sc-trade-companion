package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CompanionThemeTest {
  @Test
  void whenUserAgentStylesheetRequestedThenUseNordDarkTheme() {
    assertEquals("/atlantafx/base/theme/nord-dark.css", CompanionTheme.userAgentStylesheet());
  }

  @Test
  void whenWrappingInstructionsHtmlThenPreserveInstructionsMarkup() {
    assertTrue(CompanionTheme.wrapInstructionsHtml("<html><h2>Guide</h2></html>")
        .contains("<h2>Guide</h2>"));
  }

  @Test
  void whenWrappingInstructionsHtmlThenUseDarkBackgroundStyles() {
    assertTrue(CompanionTheme.wrapInstructionsHtml("<html><p>Guide</p></html>")
        .contains("background-color: #2E3440"));
  }
}
