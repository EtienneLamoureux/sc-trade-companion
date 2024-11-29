package tools.sctrade.companion.domain.gamelog;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;

public class GameLogPathSubject extends FilePathSubject {
  static final String GAME_LOG_FILE = "Game.log";

  private final Logger logger = LoggerFactory.getLogger(GameLogPathSubject.class);

  private SettingRepository settings;

  public GameLogPathSubject(SettingRepository settings) {
    super();
    this.settings = settings;
    this.observers = new ArrayList<>();
    setState();
  }

  public void setStarCitizenLivePath(String starCitizenLivePath) {
    if (starCitizenLivePath == null || starCitizenLivePath.strip().isEmpty()) {
      logger.warn("Star Citizen LIVE path is empty");
      return;
    }

    starCitizenLivePath = starCitizenLivePath.strip();
    settings.set(Setting.STAR_CITIZEN_LIVE_PATH, starCitizenLivePath.replace("\\", "\\\\"));
    setState();
  }

  public Optional<String> getStarCitizenLivePath() {
    return Optional.ofNullable(settings.get(Setting.STAR_CITIZEN_LIVE_PATH).toString());
  }

  @Override
  protected void setState() {
    if (getStarCitizenLivePath().isEmpty()) {
      return;
    }

    filePath = Path.of(getStarCitizenLivePath().get(), GAME_LOG_FILE);
    notifyObservers();
  }
}
