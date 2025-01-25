package tools.sctrade.companion.utils;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for working with {@link GraphicsDevice}, aka computer monitors.
 */
public class GraphicsDeviceUtil {
  /**
   * Get a {@link GraphicsDevice} by its ID.
   *
   * @param id The ID of the {@link GraphicsDevice}.
   * @return The {@link GraphicsDevice} with the given ID.
   */
  public static GraphicsDevice get(String id) {
    return getAllById().get(id);
  }

  public static String getPrimaryId() {
    return getPrimary().getIDstring();
  }

  public static GraphicsDevice getPrimary() {
    return getAll().iterator().next();
  }

  public static Collection<String> getIds() {
    return getAll().stream().map(n -> n.getIDstring()).toList();
  }

  public static Map<String, GraphicsDevice> getAllById() {
    return getAll().stream().collect(Collectors.toMap(n -> n.getIDstring(), n -> n));
  }

  public static Collection<GraphicsDevice> getAll() {
    return Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices());
  }

  private GraphicsDeviceUtil() {
    // Intentionally empty
  }
}
