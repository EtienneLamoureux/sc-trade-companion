package tools.sctrade.companion.domain.setting;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.utils.CsvUtil;

public class SettingRepository {
  private static final Collection<Setting> USER_DEFINED =
      Arrays.asList(Setting.USERNAME, Setting.STAR_CITIZEN_LIVE_PATH, Setting.STAR_CITIZEN_MONITOR);
  private final Logger logger = LoggerFactory.getLogger(SettingRepository.class);

  private Map<Setting, String> settings;
  private Path filePath;

  public SettingRepository() {
    this.settings = new EnumMap<>(Setting.class);
    filePath = Paths.get(".", "settings").normalize().toAbsolutePath();

    loadUserDefinedSettingsFromDisk();
  }

  public <T> T get(Setting setting) {
    return setting.cast(settings.get(setting));
  }

  public <T> T get(Setting setting, T defaultValue) {
    try {
      return setting.cast(settings.get(setting));
    } catch (Exception e) {
      logger.warn("Could not retreive the value of the {} setting", setting);
      return defaultValue;
    }
  }

  public void set(Setting setting, Object value) {
    settings.put(setting, String.valueOf(value));

    if (USER_DEFINED.contains(setting)) {
      saveUserDefinedSettingsToDisk();
    }
  }

  private void loadUserDefinedSettingsFromDisk() {
    var userDefinedSettingNames = Arrays.stream(Setting.values())
        .filter(n -> USER_DEFINED.contains(n)).map(n -> n.toString()).toList();

    try {
      logger.debug("Loading user-defined settings from '{}'...", filePath);
      var lines = CsvUtil.read(filePath, false);
      var userDefinedSettings =
          lines.parallelStream().filter(n -> userDefinedSettingNames.contains(n.get(0)))
              .collect(Collectors.toMap(n -> Setting.valueOf(n.get(0)), n -> n.get(1)));
      settings.putAll(userDefinedSettings);
      logger.info("Loaded user-defined settings from '{}'", filePath);
    } catch (Exception e) {
      logger.warn("Error while loading user-defined settings");
    }
  }

  private void saveUserDefinedSettingsToDisk() {
    try {
      logger.debug("Saving user-defined settings to '{}'...", filePath);
      Collection<List<String>> lines =
          settings.entrySet().parallelStream().filter(n -> USER_DEFINED.contains(n.getKey()))
              .map(n -> Arrays.asList(n.getKey().toString(), n.getValue())).toList();
      CsvUtil.write(filePath, lines, false);
      logger.info("Saved user defined settings to '{}'", filePath);
    } catch (Exception e) {
      logger.warn("Could not save-user-defined settings to file", e);
    }
  }
}
