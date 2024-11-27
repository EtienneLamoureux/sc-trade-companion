package tools.sctrade.companion.domain.user;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.utils.HashUtil;

public class UserService {
  private final Logger logger = LoggerFactory.getLogger(UserService.class);

  private SettingRepository settings;
  private User user;

  public UserService(SettingRepository settings) {
    this.settings = settings;
  }

  public User get() {
    if (user == null) {
      user = new User(getId(), settings.get(Setting.USERNAME));
    }

    return user;
  }

  public void updateUsername(String username) {
    if (username == null || username.strip().isEmpty()) {
      logger.warn("Username is empty");
      return;
    }

    username = username.strip();
    settings.set(Setting.USERNAME, username);
    user = get().withLabel(username);
  }

  private String getId() {
    String id;

    try {
      id = getSystemUuid();
    } catch (Exception e) {
      logger.warn("Could not retrieve computer identifier", e);
      UUID uuid = UUID.randomUUID();
      id = uuid.toString();
    }

    return HashUtil.hash(id);
  }

  private String getSystemUuid() throws Exception {
    String system = System.getProperty("os.name").toLowerCase();

    if (system.indexOf("win") >= 0) {
      String[] cmd = {"wmic", "csproduct", "get", "UUID"};
      Process process = Runtime.getRuntime().exec(cmd);
      process.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line = "";
      StringBuilder output = new StringBuilder();

      while ((line = reader.readLine()) != null) {
        output.append(line);
      }

      return output.toString().replaceAll("\\s+", " ").strip();
    } else {
      throw new RuntimeException("System is not Windows");
    }
  }
}
