package tools.sctrade.companion.gui.screenshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import tools.sctrade.companion.utils.patterns.Subject;

/**
 * Thread-safe, in-memory ordered repository for {@link Screenshot} instances.
 *
 * <p>
 * New screenshots are inserted at the head; updates to existing entries are applied in-place,
 * preserving their current position. The repository is capped at {@value #MAX_SIZE} entries; any
 * entries beyond that limit are silently dropped from the tail.
 */
public class ScreenshotRepository extends Subject<List<Screenshot>> {

  private static final int MAX_SIZE = 36;

  private final LinkedList<Screenshot> screenshots = new LinkedList<>();

  /**
   * Creates a new screenshot repository.
   */
  public ScreenshotRepository() {
    this.observers = new ArrayList<>();
    this.state = Collections.unmodifiableList(new ArrayList<>());
  }

  /**
   * Inserts or replaces a {@link Screenshot} by its {@code id}.
   *
   * <p>
   * If no entry with the same {@code id} exists the screenshot is inserted at the head. If an entry
   * with the same {@code id} already exists it is replaced in-place, preserving its current
   * position. Entries beyond {@value #MAX_SIZE} are dropped from the tail.
   *
   * @param screenshot Screenshot to insert or replace.
   */
  public synchronized void upsert(Screenshot screenshot) {
    int index = -1;
    for (int i = 0; i < screenshots.size(); i++) {
      if (screenshots.get(i).id().equals(screenshot.id())) {
        index = i;
        break;
      }
    }

    if (index >= 0) {
      screenshots.set(index, screenshots.get(index).updateUsing(screenshot));
    } else {
      screenshots.addFirst(screenshot);
      while (screenshots.size() > MAX_SIZE) {
        screenshots.removeLast();
      }
    }

    setState();
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

  /**
   * Returns repository capacity.
   *
   * @return maximum number of screenshots kept in repository
   */
  public int getCapacity() {
    return MAX_SIZE;
  }

  @Override
  protected synchronized void setState() {
    this.state = getSnapshot();
    notifyObservers();
  }
}
