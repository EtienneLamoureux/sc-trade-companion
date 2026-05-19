package tools.sctrade.companion.domain.screenshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Thread-safe, in-memory ordered repository for {@link Screenshot} instances.
 *
 * <p>
 * Entries are kept in insertion order with the most-recently upserted screenshot at the head. The
 * repository is capped at {@value #MAX_SIZE} entries; any entries beyond that limit are silently
 * dropped from the tail.
 */
public class ScreenshotRepository {

  private static final int MAX_SIZE = 36;

  private final LinkedList<Screenshot> screenshots = new LinkedList<>();

  /**
   * Inserts or replaces a {@link Screenshot} by its {@code id}.
   *
   * <p>
   * If no entry with the same {@code id} exists the screenshot is inserted at the head. If an entry
   * with the same {@code id} already exists it is replaced with the supplied value and moved to the
   * head. Entries beyond {@value #MAX_SIZE} are dropped from the tail.
   *
   * @param screenshot Screenshot to insert or replace.
   */
  public synchronized void upsert(Screenshot screenshot) {
    screenshots.removeIf(s -> s.id().equals(screenshot.id()));
    screenshots.addFirst(screenshot);

    while (screenshots.size() > MAX_SIZE) {
      screenshots.removeLast();
    }
  }

  /**
   * Returns an immutable, point-in-time snapshot of all stored screenshots ordered from most to
   * least recently upserted.
   *
   * @return Unmodifiable list of screenshots.
   */
  public synchronized List<Screenshot> getSnapshot() {
    return Collections.unmodifiableList(new ArrayList<>(screenshots));
  }
}
