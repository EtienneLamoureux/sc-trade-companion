package tools.sctrade.companion.gui;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;

/**
 * Shared theme helpers for the companion JavaFX application.
 */
public final class CompanionTheme {
  private static final NordDark NORD_DARK = new NordDark();
  private static final String INSTRUCTIONS_STYLE = """
      <head><meta charset="UTF-8"><style>
      html, body {
        margin: 0;
        padding: 0;
        background-color: #2E3440;
        color: #ECEFF4;
        font-family: "Segoe UI", sans-serif;
        line-height: 1.5;
      }
      body {
        padding: 16px 18px 20px;
      }
      a { color: #88C0D0; }
      code, kbd {
        background-color: #3B4252;
        color: #ECEFF4;
        border-radius: 4px;
        padding: 1px 4px;
      }
      strong, b { color: #E5E9F0; }
      h2, h3 { color: #ECEFF4; }
      </style></head><body>""";
  private static final String HTML_OPEN = "<html>";
  private static final String HTML_CLOSE = "</html>";

  private CompanionTheme() {}

  /**
   * Applies the companion user-agent stylesheet globally.
   */
  public static void applyUserAgentStylesheet() {
    Application.setUserAgentStylesheet(userAgentStylesheet());
  }

  /**
   * Returns the AtlantaFX Nord Dark user-agent stylesheet path.
   *
   * @return the Nord Dark stylesheet path
   */
  public static String userAgentStylesheet() {
    return NORD_DARK.getUserAgentStylesheet();
  }

  static String wrapInstructionsHtml(String instructionsHtml) {
    String content = instructionsHtml == null ? "" : instructionsHtml.trim();

    if (content.startsWith(HTML_OPEN) && content.endsWith(HTML_CLOSE)) {
      String bodyContent =
          content.substring(HTML_OPEN.length(), content.length() - HTML_CLOSE.length());
      return HTML_OPEN + INSTRUCTIONS_STYLE + bodyContent + "</body>" + HTML_CLOSE;
    }

    return HTML_OPEN + INSTRUCTIONS_STYLE + content + "</body>" + HTML_CLOSE;
  }
}
