package tools.sctrade.companion.domain.gamelog;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;

public class GameLogService {
  private static final String GAME_LOG = "\\Game.log";

  private final Logger logger = LoggerFactory.getLogger(GameLogService.class);

  private SettingRepository settings;

  public GameLogService(SettingRepository settings) {
    this.settings = settings;
  }

  public void updateStarCitizenLivePath(String starCitizenLivePath) {
    if (starCitizenLivePath == null || starCitizenLivePath.strip().isEmpty()) {
      logger.warn("Star Citizen LIVE path is empty");
      return;
    }

    starCitizenLivePath = starCitizenLivePath.strip();
    settings.set(Setting.STAR_CITIZEN_LIVE_PATH, starCitizenLivePath.replace("\\", "\\\\"));
  }

  public Optional<String> getStarCitizenLivePath() {
    return Optional.ofNullable(settings.get(Setting.STAR_CITIZEN_LIVE_PATH).toString());
  }

}
