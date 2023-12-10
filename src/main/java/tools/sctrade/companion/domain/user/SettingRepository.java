package tools.sctrade.companion.domain.user;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.utils.CsvUtil;

public class SettingRepository {
  private static final Collection<Setting> USER_DEFINED = Arrays.asList(Setting.USERNAME);
  private final Logger logger = LoggerFactory.getLogger(SettingRepository.class);

  private Map<Setting, String> settings;

  public SettingRepository() {
    this.settings = new EnumMap<>(Setting.class);
  }

  public <T> T get(Setting setting) {
    return setting.cast(settings.get(setting));
  }

  public void set(Setting setting, Object value) {
    settings.put(setting, String.valueOf(value));

    if (USER_DEFINED.contains(setting)) {
      saveUserDefinedSettingsToFile();
    }
  }

  private void saveUserDefinedSettingsToFile() {
    try {
      Collection<List<String>> lines =
          settings.entrySet().parallelStream().filter(n -> USER_DEFINED.contains(n.getKey()))
              .map(n -> Arrays.asList(n.getKey().toString(), n.getValue())).toList();
      CsvUtil.write(Paths.get(".", "settings").normalize().toAbsolutePath(), lines, false);
    } catch (Exception e) {
      logger.warn("Could not save user-defined settings to file", e);
    }
  }
}
