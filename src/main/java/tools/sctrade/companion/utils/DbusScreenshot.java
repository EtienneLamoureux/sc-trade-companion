package tools.sctrade.companion.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import javax.imageio.ImageIO;
import org.freedesktop.dbus.DBusMatchRule;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for taking screenshots via the XDG Desktop Portal D-Bus API. This is the standard
 * mechanism for screen capture on Wayland compositors, where {@link java.awt.Robot} does not have
 * direct access to the display.
 *
 * <p>
 * The portal's {@code interactive} option is set to {@code false}, requesting a non-interactive
 * capture. Most compositors (KDE, wlroots) honour this; GNOME may still show a dialog.
 */
class DbusScreenshot {
  private static final Logger logger = LoggerFactory.getLogger(DbusScreenshot.class);

  private static final String PORTAL_BUS_NAME = "org.freedesktop.portal.Desktop";
  private static final String PORTAL_OBJECT_PATH = "/org/freedesktop/portal/desktop";
  private static final long SCREENSHOT_TIMEOUT_SECONDS = 30;

  private DbusScreenshot() {
    // Utility class
  }

  /**
   * Takes a screenshot via the XDG Desktop Portal.
   *
   * @return the captured image
   * @throws ScreenshotException if the screenshot could not be taken
   */
  static BufferedImage capture() {
    String token = UUID.randomUUID().toString().replace("-", "");

    try {
      DBusConnection bus = DBusConnectionBuilder.forSessionBus().build();
      try {
        return captureViaPortal(bus, token);
      } finally {
        bus.disconnect();
      }
    } catch (DBusException e) {
      throw new ScreenshotException("Failed to connect to session D-Bus", e);
    }
  }

  private static BufferedImage captureViaPortal(DBusConnection bus, String token)
      throws DBusException {
    String sender = bus.getUniqueName().substring(1).replace('.', '_');
    String expectedPath =
        String.format("/org/freedesktop/portal/desktop/request/%s/%s", sender, token);

    TransferQueue<Optional<BufferedImage>> queue = new LinkedTransferQueue<>();

    DBusMatchRule matchRule =
        new DBusMatchRule("signal", "org.freedesktop.portal.Request", "Response");

    DBusSigHandler<DBusSignal> handler = signal -> {
      if (!expectedPath.equals(signal.getPath())) {
        return;
      }
      handleResponse(signal, queue, matchRule, bus);
    };

    bus.addGenericSigHandler(matchRule, handler);

    try {
      ScreenshotPortal portal =
          bus.getRemoteObject(PORTAL_BUS_NAME, PORTAL_OBJECT_PATH, ScreenshotPortal.class);

      Map<String, Variant<?>> options = new HashMap<>();
      options.put("interactive", new Variant<>(Boolean.FALSE));
      options.put("handle_token", new Variant<>(token));

      portal.Screenshot("", options);

      Optional<BufferedImage> result = queue.poll(SCREENSHOT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      if (result == null) {
        throw new ScreenshotException("Screenshot timed out after " + SCREENSHOT_TIMEOUT_SECONDS
            + " seconds waiting for portal response");
      }
      return result.orElseThrow(() -> new ScreenshotException("Portal returned an error response"));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ScreenshotException("Screenshot interrupted", e);
    } finally {
      bus.removeGenericSigHandler(matchRule, handler);
    }
  }

  @SuppressWarnings("unchecked")
  private static void handleResponse(DBusSignal signal,
      TransferQueue<Optional<BufferedImage>> queue, DBusMatchRule matchRule, DBusConnection bus) {
    try {
      Object[] params = signal.getParameters();
      UInt32 response = (UInt32) params[0];
      LinkedHashMap<String, Variant<?>> results = (LinkedHashMap<String, Variant<?>>) params[1];

      if (response.intValue() != 0) {
        logger.error("Portal screenshot failed with response code: {}", response);
        queue.add(Optional.empty());
        return;
      }

      Variant<?> uriVariant = results.get("uri");
      String uri = (String) uriVariant.getValue();
      logger.debug("Screenshot URI: {}", uri);

      BufferedImage image = ImageIO.read(URI.create(uri).toURL());
      queue.add(Optional.ofNullable(image));

      // Clean up the temporary file created by the portal.
      Path tempFile = Path.of(URI.create(uri));
      if (Files.deleteIfExists(tempFile)) {
        logger.debug("Deleted portal temp file: {}", tempFile);
      }
    } catch (IOException | DBusException e) {
      logger.error("Error reading screenshot from portal", e);
      queue.add(Optional.empty());
    }
  }

  /**
   * Exception thrown when a screenshot cannot be taken via the D-Bus portal.
   */
  static class ScreenshotException extends RuntimeException {
    ScreenshotException(String message) {
      super(message);
    }

    ScreenshotException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
