package tools.sctrade.companion.gui.screenshot;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.commodity.CommodityListing;
import tools.sctrade.companion.domain.commodity.CommoditySubmission;
import tools.sctrade.companion.domain.item.ItemListing;
import tools.sctrade.companion.domain.item.ItemSubmission;

/**
 * Builds screenshot records for processing lifecycle events.
 */
public class ScreenshotFactory {

  /**
   * Builds a processing-status screenshot record.
   *
   * @param id screenshot id
   * @param image original screenshot image
   * @param type screenshot type
   * @return processing-status screenshot record
   */
  public Screenshot buildProcessing(String id, BufferedImage image, ScreenshotType type) {
    return new Screenshot(id, image, null, ScreenshotStatus.PROCESSING, null, null, type);
  }

  /**
   * Builds a success-status screenshot record.
   *
   * @param id screenshot id
   * @param image original screenshot image
   * @param submission decoded submission object
   * @param type screenshot type
   * @return success-status screenshot record
   */
  public Screenshot buildSuccess(String id, BufferedImage image, Object submission,
      ScreenshotType type) {
    return new Screenshot(id, image, extractLocation(submission), ScreenshotStatus.SUCCESS, null,
        extractContent(submission), type);
  }

  /**
   * Builds an error-status screenshot record.
   *
   * @param id screenshot id
   * @param image original screenshot image
   * @param exception exception raised while processing
   * @param type screenshot type
   * @return error-status screenshot record
   */
  public Screenshot buildError(String id, BufferedImage image, RuntimeException exception,
      ScreenshotType type) {
    return new Screenshot(id, image, null, ScreenshotStatus.ERROR, exception.getMessage(), null,
        type);
  }

  private String extractLocation(Object submission) {
    if (submission instanceof CommoditySubmission commoditySubmission) {
      return commoditySubmission.getListings().stream().map(CommodityListing::location)
          .filter(l -> l != null).findFirst().orElse(null);
    }

    if (submission instanceof ItemSubmission itemSubmission) {
      return itemSubmission.getListings().stream().map(ItemListing::location).filter(l -> l != null)
          .findFirst().orElse(null);
    }

    return null;
  }

  private String extractContent(Object submission) {
    if (submission instanceof CommoditySubmission commoditySubmission) {
      return commoditySubmission.getListings().size() + " listings processed";
    }

    if (submission instanceof ItemSubmission itemSubmission) {
      return itemSubmission.getListings().size() + " listings processed";
    }

    return null;
  }
}
