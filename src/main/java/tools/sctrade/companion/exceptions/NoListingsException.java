package tools.sctrade.companion.exceptions;

import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Exception thrown when no listings are found in the image.
 */
public class NoListingsException extends RuntimeException {
  private static final long serialVersionUID = -3711903740803747206L;

  /**
   * Creates a new instance of the NoListingsException class.
   */
  public NoListingsException() {
    super(LocalizationUtil.get("errorNoListings"));
  }
}
