package tools.sctrade.companion.utils;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for taking screenshots. On Wayland, uses the XDG Desktop Portal via D-Bus and crops the
 * result to the requested monitor's bounds. On X11 and Windows, uses {@link Robot} with the
 * specified monitor.
 */
public class ScreenshotUtil {
  private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtil.class);

  private ScreenshotUtil() {
    // Utility class
  }

  /**
   * Takes a screenshot of the specified monitor. On Wayland, the XDG Desktop Portal captures all
   * monitors, so the result is cropped to the requested monitor's bounds.
   *
   * @param monitor the graphics device to capture
   * @return the captured image
   * @throws AWTException if the Robot cannot be created (X11/Windows only)
   */
  public static BufferedImage capture(GraphicsDevice monitor) throws AWTException {
    if (isWayland()) {
      return captureWayland(monitor);
    }
    return captureRobot(monitor);
  }

  private static boolean isWayland() {
    return "wayland".equalsIgnoreCase(System.getenv("XDG_SESSION_TYPE"));
  }

  private static BufferedImage captureWayland(GraphicsDevice monitor) {
    logger.debug("Taking screenshot via XDG Desktop Portal (Wayland)");
    try {
      BufferedImage fullCapture = DbusScreenshot.capture();
      return cropToMonitor(fullCapture, monitor);
    } catch (DbusScreenshot.ScreenshotException e) {
      logger.error("D-Bus screenshot failed, falling back to Robot", e);
      try {
        return captureRobot(monitor);
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

  /**
   * Crops a full-desktop screenshot to the bounds of the specified monitor. If the monitor bounds
   * fall outside the image (e.g. single-monitor setup), the original image is returned.
   */
  private static BufferedImage cropToMonitor(BufferedImage fullCapture, GraphicsDevice monitor) {
    Rectangle bounds = monitor.getDefaultConfiguration().getBounds();
    int x = Math.max(0, bounds.x);
    int y = Math.max(0, bounds.y);
    int width = Math.min(bounds.width, fullCapture.getWidth() - x);
    int height = Math.min(bounds.height, fullCapture.getHeight() - y);

    if (x == 0 && y == 0 && width == fullCapture.getWidth() && height == fullCapture.getHeight()) {
      return fullCapture;
    }

    logger.debug("Cropping portal screenshot to monitor bounds: {}x{} at ({},{})", width, height, x,
        y);
    return fullCapture.getSubimage(x, y, width, height);
  }
}
