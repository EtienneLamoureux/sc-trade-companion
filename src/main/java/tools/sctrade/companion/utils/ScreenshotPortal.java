package tools.sctrade.companion.utils;

import java.util.Map;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

/**
 * D-Bus interface binding for the XDG Desktop Portal Screenshot API.
 *
 * @see <a href=
 *      "https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.Screenshot.html">XDG
 *      Desktop Portal - Screenshot</a>
 */
@DBusInterfaceName("org.freedesktop.portal.Screenshot")
interface ScreenshotPortal extends DBusInterface {

  /**
   * Takes a screenshot of the current screen.
   *
   * @param parentWindow identifier for the application window (empty string for none)
   * @param options a map of options; notable keys include {@code interactive} (boolean) and
   *        {@code handle_token} (string)
   * @return a D-Bus object path for the portal request
   */
  DBusPath Screenshot(String parentWindow, Map<String, Variant<?>> options);
}
