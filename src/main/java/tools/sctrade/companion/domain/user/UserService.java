package tools.sctrade.companion.domain.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;

/**
 * Service for managing the user of this app.
 */
public class UserService {
  private final Logger logger = LoggerFactory.getLogger(UserService.class);

  private SettingRepository settings;
  private UserIdGenerator userIdGenerator;
  private User user;

  /**
   * Creates a new user service.
   *
   * @param settings The repository for settings
   * @param userIdGenerator User ID generator
   */
  public UserService(SettingRepository settings, UserIdGenerator userIdGenerator) {
    this.settings = settings;
    this.userIdGenerator = userIdGenerator;
  }

  /**
   * Returns the user.
   *
   * @return The user.
   */
  public User get() {
    if (user == null) {
      user = new User(userIdGenerator.getId(), settings.get(Setting.USERNAME));
    }

    return user;
  }

  /**
   * Updates the username of the user.
   *
   * @param username The new username.
   */
  public void updateUsername(String username) {
    if (username == null || username.strip().isEmpty()) {
      logger.warn("Username is empty");
      return;
    }

    username = username.strip();
    settings.set(Setting.USERNAME, username);
    user = get().withLabel(username);
  }
}
