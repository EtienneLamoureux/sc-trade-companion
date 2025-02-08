package tools.sctrade.companion.exceptions;

import tools.sctrade.companion.domain.commodity.CommoditySubmission;

/**
 * Thrown when an error occurs while publishing the final {@link CommoditySubmission}.
 */
public class PublicationException extends RuntimeException {
  private static final long serialVersionUID = 2799328251052287898L;

  /**
   * Constructor for {@link PublicationException}.
   *
   * @param message Error message
   */
  public PublicationException(String message) {
    super(message);
  }

}
