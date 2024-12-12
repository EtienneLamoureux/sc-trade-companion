package tools.sctrade.companion.domain.gamelog;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.utils.patterns.Subject;

public class GameLogPathSubject extends Subject<Path> {
  static final String GAME_LOG_FILE = "Game.log";

  private final Logger logger = LoggerFactory.getLogger(GameLogPathSubject.class);

  private SettingRepository settings;

  public GameLogPathSubject(SettingRepository settings) {
    super();
    this.settings = settings;
    this.observers = new ArrayList<>();
  }

  public void setStarCitizenLivePath(String starCitizenLivePath) {
    if (starCitizenLivePath == null || starCitizenLivePath.strip().isEmpty()) {
      logger.warn("Star Citizen LIVE path is empty");
      return;
    }

    settings.set(Setting.STAR_CITIZEN_LIVE_PATH, starCitizenLivePath);
    setState();
  }

  public Optional<String> getStarCitizenLivePath() {
    return Optional.ofNullable(settings.get(Setting.STAR_CITIZEN_LIVE_PATH));
  }

  @Override
  protected void setState() {
    if (getStarCitizenLivePath().isEmpty()) {
      return;
    }

    state = Path.of(getStarCitizenLivePath().get(), GAME_LOG_FILE);
    notifyObservers();
  }
}
