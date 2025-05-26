package tools.sctrade.companion.domain.user.idgenerators;

import java.util.List;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import tools.sctrade.companion.domain.user.UserIdGenerator;

/**
 * Adapter around <code>oshi.hardware</code> used to generate a user id from the machine's hardware.
 */
public class HardwareUserIdGenerator extends UserIdGenerator {
  private HardwareAbstractionLayer hardware;

  /**
   * Default constructor.
   */
  public HardwareUserIdGenerator() {
    hardware = new SystemInfo().getHardware();
  }

  @Override
  protected String generateId() {
    CentralProcessor processor = hardware.getProcessor();
    String processorId = processor.getProcessorIdentifier().getProcessorID();

    ComputerSystem computerSystem = hardware.getComputerSystem();
    String motherboardSerial = computerSystem.getBaseboard().getSerialNumber();

    List<HWDiskStore> diskStores = hardware.getDiskStores();
    String diskSerial = diskStores.stream().findFirst().map(n -> n.getSerial()).orElse("");

    return processorId + motherboardSerial + diskSerial;
  }

}
