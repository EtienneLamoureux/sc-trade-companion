package tools.sctrade.companion.domain.user;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
<<<<<<< HEAD
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
=======

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
>>>>>>> cbb30bd (UserService.java: Replace wmic invokation for a cross-platform solution based on computer hardware serial numbers.)
import tools.sctrade.companion.utils.HashUtil;

/**
 * Service for managing the user of this app.
 */
public class UserService {
  private final Logger logger = LoggerFactory.getLogger(UserService.class);

  private SettingRepository settings;
  private User user;

  /**
   * Creates a new user service.
   *
   * @param settings The repository for settings
   */
  public UserService(SettingRepository settings) {
    this.settings = settings;
  }

  /**
   * Returns the user.
   *
   * @return The user.
   */
  public User get() {
    if (user == null) {
      user = new User(getId(), settings.get(Setting.USERNAME));
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

  private String getId() {
    SystemInfo si = new SystemInfo();
    HardwareAbstractionLayer hal = si.getHardware();
    CentralProcessor processor = hal.getProcessor();
    ComputerSystem computerSystem = hal.getComputerSystem();
    List<HWDiskStore> diskStores = hal.getDiskStores();

    // Get CPU ID
    String processorId = processor.getProcessorIdentifier().getProcessorID();

    // Get Motherboard Serial Number
    String motherboardSerial = computerSystem.getBaseboard().getSerialNumber();

    // Get Disk Serial Number (use first disk's serial number)
    String diskSerial = "";
    if (!diskStores.isEmpty()) {
      diskSerial = diskStores.get(0).getSerial();  // Access first item in the list
    }

    // Combine hardware components into a string
    String hardwareString = processorId + motherboardSerial + diskSerial;

    // Generate hashed UUID based on the hardware components
    return HashUtil.hash(UUID.nameUUIDFromBytes(hardwareString.getBytes()).toString());
  }
}
