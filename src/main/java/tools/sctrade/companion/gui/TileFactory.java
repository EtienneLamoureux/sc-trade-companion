package tools.sctrade.companion.gui;

import atlantafx.base.controls.Tile;

/**
 * Generic contract for building UI tiles from typed input payloads.
 *
 * @param <T> tile input payload type
 */
public interface TileFactory<T> {

  /**
   * Builds a tile from input data.
   *
   * @param input factory input payload
   * @return tile node
   */
  Tile build(T input);
}
