package tools.sctrade.companion.domain.user;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
      id = getMacAddress();
    } catch (Exception e) {
      logger.warn("Could not retreive MAC address", e);
      UUID uuid = UUID.randomUUID();
      id = uuid.toString();
    }

    return HashUtil.hash(id);
  }

  private String getMacAddress() throws UnknownHostException, SocketException {
    InetAddress localHost = InetAddress.getLocalHost();
    NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
    byte[] hardwareAddress = ni.getHardwareAddress();
    String[] hexadecimal = new String[hardwareAddress.length];

    for (int i = 0; i < hardwareAddress.length; i++) {
      hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
    }

    return String.join("-", hexadecimal);
  }
}
