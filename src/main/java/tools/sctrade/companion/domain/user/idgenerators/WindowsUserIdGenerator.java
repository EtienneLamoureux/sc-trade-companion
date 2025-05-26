package tools.sctrade.companion.domain.user.idgenerators;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import tools.sctrade.companion.domain.user.UserIdGenerator;
import tools.sctrade.companion.exceptions.UserIdGenerationException;

/**
 * Generates the user id from the BIOS serial number, acquired through native commands.
 */
public class WindowsUserIdGenerator extends UserIdGenerator {
  @Override
  protected String generateId() {
    assertIsWindows();

    try {
      return getWmicCsProductUuid();
    } catch (UserIdGenerationException e) {
      return getPowershellSerialNumber();
    }
  }

  private void assertIsWindows() {
    String system = System.getProperty("os.name").toLowerCase();

    if (!system.contains("win")) {
      throw new UserIdGenerationException("System is not Windows");
    }
  }

  /**
   * Runs <code>wmic csproduct get UUID</code>on the local system. This command has been deprecated
   * in Windows 11 24H2.
   *
   * @return The serial number of the BIOS
   */
  private String getWmicCsProductUuid() {
    String[] cmd = {"wmic", "csproduct", "get", "UUID"};

    return runCommand(cmd);
  }

  /**
   * Runs <code>powershell.exe (Get-CimInstance -ClassName Win32_BIOS).SerialNumber</code> on the
   * local system.
   *
   * @return The serial number of the BIOS
   */
  private String getPowershellSerialNumber() {
    String[] cmd = {"powershell.exe", "(Get-CimInstance -ClassName Win32_BIOS).SerialNumber"};

    return runCommand(cmd);
  }

  private String runCommand(String[] cmd) {
    try {
      Process process = Runtime.getRuntime().exec(cmd);
      process.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line = "";
      StringBuilder output = new StringBuilder();

      while ((line = reader.readLine()) != null) {
        output.append(line);
      }

      return output.toString().replaceAll("\\s+", " ").strip();
    } catch (Exception e) {
      throw new UserIdGenerationException(e);
    }
  }
}
