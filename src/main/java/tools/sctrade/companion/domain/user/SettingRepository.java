package tools.sctrade.companion.domain.user;

import java.util.EnumMap;
import java.util.Map;

public class SettingRepository {
  private Map<Setting, String> settings;

  public SettingRepository() {
    this.settings = new EnumMap<>(Setting.class);
  }

  public <T> T get(Setting setting) {
    return setting.cast(settings.get(setting));
  }

  public void set(Setting setting, Object value) {
    settings.put(setting, String.valueOf(value));
  }
}
