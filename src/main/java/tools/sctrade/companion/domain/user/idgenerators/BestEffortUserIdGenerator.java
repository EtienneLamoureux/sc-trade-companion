package tools.sctrade.companion.domain.user.idgenerators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.user.UserIdGenerator;

/**
 * Decorator around multiple implementations of {@link UserIdGenerator}. For backwards compatibility
 * reasons, always tries to generate the user id from Windows commands first.
 */
public class BestEffortUserIdGenerator extends UserIdGenerator {
  private final Logger logger = LoggerFactory.getLogger(BestEffortUserIdGenerator.class);

  private WindowsUserIdGenerator windowsGenerator;
  private HardwareUserIdGenerator hardwareGenerator;
  private RandomUserIdGenerator randomGenerator;

  /**
   * Constructor.
   *
   * @param windowsGenerator {@link WindowsUserIdGenerator}
   * @param hardwareGenerator {@link HardwareUserIdGenerator}
   * @param randomGenerator {@link RandomUserIdGenerator}
   */
  public BestEffortUserIdGenerator(WindowsUserIdGenerator windowsGenerator,
      HardwareUserIdGenerator hardwareGenerator, RandomUserIdGenerator randomGenerator) {
    this.windowsGenerator = windowsGenerator;
    this.hardwareGenerator = hardwareGenerator;
    this.randomGenerator = randomGenerator;
  }

  @Override
  protected String generateId() {
    try {
      logger.debug("Generating user id from Windows commands...");
      return windowsGenerator.generateId();
    } catch (Exception e) {
      try {
        logger.error(
            "Error while generating user id from Windows command, falling back on hardware", e);
        return hardwareGenerator.generateId();
      } catch (Exception ex) {
        logger.error(
            "Error while generating user id from hardware, falling back temporary random id", ex);
        return randomGenerator.generateId();
      }
    }
  }

}
