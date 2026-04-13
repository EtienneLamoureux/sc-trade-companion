package tools.sctrade.companion.output;

/**
 * Repository for fetching the latest published version of SC Trade Companion from sc-trade.tools.
 */
public interface ScTradeToolsCompanionVersionRepository {

  /**
   * Returns the latest available version string (e.g. {@code "1.2.3"}).
   *
   * @return the latest version string
   */
  String fetchLatestVersion();
}
