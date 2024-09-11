package tools.sctrade.companion.utils;

import java.util.Map;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

@DBusInterfaceName(value = "org.freedesktop.portal.Screenshot")
public interface ScreenshotInterface extends DBusInterface {

  // https://flatpak.github.io/xdg-desktop-portal/portal-docs.html#gdbus-org.freedesktop.portal.Screenshot
  // Screenshot (IN  s     parent_window,
  //        IN  a{sv} options,
  //        OUT o     handle);
  DBusPath Screenshot(String parentWindow, Map<String, Variant<?>> options);
}
