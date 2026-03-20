package tools.sctrade.companion.domain.item;

import java.util.List;

/**
 * Repository for items.
 */
public interface ItemRepository {
  /**
   * Returns a list of all items' names.
   *
   * @return List of items' names.
   */
  public List<String> findAllItems();
}
