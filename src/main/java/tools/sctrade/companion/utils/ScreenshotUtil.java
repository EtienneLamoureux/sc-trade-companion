package tools.sctrade.companion.utils;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for taking screenshots. On Wayland, uses the XDG Desktop Portal via D-Bus. On X11 and
 * Windows, uses {@link Robot} with the specified monitor.
 */
public class ScreenshotUtil {
  private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtil.class);

  private ScreenshotUtil() {
    // Utility class
  }

  /**
   * Takes a screenshot of the specified monitor. On Wayland, the monitor parameter is ignored
   * because the XDG Desktop Portal captures the entire screen.
   *
   * @param monitor the graphics device to capture (used on X11/Windows only)
   * @return the captured image
   * @throws AWTException if the Robot cannot be created (X11/Windows only)
   */
  public static BufferedImage capture(GraphicsDevice monitor) throws AWTException {
    if (isWayland()) {
      return captureWayland();
    }
    return captureRobot(monitor);
  }

  private static boolean isWayland() {
    return "wayland".equalsIgnoreCase(System.getenv("XDG_SESSION_TYPE"));
  }

  private static BufferedImage captureWayland() {
    logger.debug("Taking screenshot via XDG Desktop Portal (Wayland)");
    try {
      return DbusScreenshot.capture();
    } catch (DbusScreenshot.ScreenshotException e) {
      logger.error("D-Bus screenshot failed, falling back to Robot", e);
      try {
        return captureRobot(GraphicsDeviceUtil.getPrimary());
      } catch (AWTException ex) {
        throw new IllegalStateException("Both D-Bus and Robot screenshot methods failed", ex);
      }
    }
  }

  private static BufferedImage captureRobot(GraphicsDevice monitor) throws AWTException {
    logger.debug("Taking screenshot via Robot (monitor: {})", monitor.getIDstring());
    var screenRectangle = monitor.getDefaultConfiguration().getBounds();
    return new Robot(monitor).createScreenCapture(screenRectangle);
  }
}
