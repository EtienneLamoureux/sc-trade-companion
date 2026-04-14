package tools.sctrade.companion.domain.version;

/**
 * Repository port for fetching the latest published version of SC Trade Companion.
 */
public interface CompanionVersionRepository {
  /**
   * Fetches the latest available version string.
   *
   * @return the latest version string (e.g. {@code "1.2.3"})
   */
  String fetchLatestVersion();
}
