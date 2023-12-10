package tools.sctrade.companion.exceptions;

import tools.sctrade.companion.utils.LocalizationUtil;

public class NoListingsException extends RuntimeException {
  private static final long serialVersionUID = -3711903740803747206L;

  public NoListingsException() {
    super(LocalizationUtil.get("errorNoListings"));
  }
}
