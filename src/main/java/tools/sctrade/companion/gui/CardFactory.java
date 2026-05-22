package tools.sctrade.companion.gui;

import javafx.scene.layout.VBox;

/**
 * Generic contract for building UI cards from typed input payloads.
 *
 * @param <T> card input payload type
 */
public interface CardFactory<T> {

  /**
   * Builds a card from input data.
   *
   * @param input factory input payload
   * @return card node
   */
  VBox build(T input);
}
