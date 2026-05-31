package tools.sctrade.companion.gui.screenshot;

import java.awt.image.BufferedImage;
import org.imgscalr.Scalr;
import tools.sctrade.companion.domain.commodity.CommodityListing;
import tools.sctrade.companion.domain.commodity.CommoditySubmission;
import tools.sctrade.companion.domain.item.ItemListing;
import tools.sctrade.companion.domain.item.ItemSubmission;
import tools.sctrade.companion.utils.HashUtil;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Builds screenshot records for processing lifecycle events.
 *
 * <p>
 * All {@code build} overloads scale the provided image to a {@value #THUMBNAIL_SIZE} px thumbnail
 * before storing it in the {@link Screenshot} record.
 */
public class ScreenshotFactory {

  static final int THUMBNAIL_SIZE = 150;

  /**
   * Builds a queued-status screenshot record.
   *
   * @param image original screenshot image
   * @param type screenshot type
   * @return queued-status screenshot record
   */
  public Screenshot buildQueued(BufferedImage image, ScreenshotType type) {
    return new Screenshot(computeId(type, image), scaleToThumbnail(image), null,
        ScreenshotStatus.QUEUED, null, null, type);
  }

  /**
   * Builds a processing-status screenshot record.
   *
   * @param image original screenshot image
   * @param type screenshot type
   * @return processing-status screenshot record
   */
  public Screenshot build(BufferedImage image, ScreenshotType type) {
    return new Screenshot(computeId(type, image), scaleToThumbnail(image), null,
        ScreenshotStatus.PROCESSING, null, null, type);
  }

  /**
   * Builds a success-status screenshot record.
   *
   * @param image original screenshot image
   * @param submission decoded submission object
   * @param type screenshot type
   * @return success-status screenshot record
   */
  public Screenshot build(BufferedImage image, Object submission, ScreenshotType type) {
    if (submission instanceof ItemSubmission itemSubmission) {
      return buildFromItemSubmission(computeId(type, image), image, itemSubmission, type);
    }

    return new Screenshot(computeId(type, image), scaleToThumbnail(image),
        extractLocation(submission), ScreenshotStatus.SUCCESS, null, extractContent(submission),
        type);
  }

  /**
   * Builds an error-status screenshot record.
   *
   * @param image original screenshot image
   * @param exception exception raised while processing
   * @param type screenshot type
   * @return error-status screenshot record
   */
  public Screenshot build(BufferedImage image, RuntimeException exception, ScreenshotType type) {
    return new Screenshot(computeId(type, image), scaleToThumbnail(image), null,
        ScreenshotStatus.ERROR, exception.getMessage(), null, type);
  }

  private Screenshot buildFromItemSubmission(String id, BufferedImage image,
      ItemSubmission itemSubmission, ScreenshotType type) {
    if (itemSubmission.isEmpty() || hasMissingLocation(itemSubmission)) {
      return new Screenshot(id, scaleToThumbnail(image), null, ScreenshotStatus.ERROR,
          LocalizationUtil.get("warnNoLocation"), null, type);
    }

    if (hasMissingShop(itemSubmission)) {
      return new Screenshot(id, scaleToThumbnail(image), null, ScreenshotStatus.ERROR,
          LocalizationUtil.get("warnNoShop"), null, type);
    }

    return new Screenshot(id, scaleToThumbnail(image), extractLocation(itemSubmission),
        ScreenshotStatus.SUCCESS, null, extractContent(itemSubmission), type);
  }

  private BufferedImage scaleToThumbnail(BufferedImage image) {
    if (image == null) {
      return null;
    }
    if (image.getWidth() <= THUMBNAIL_SIZE && image.getHeight() <= THUMBNAIL_SIZE) {
      return image;
    }
    return Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, THUMBNAIL_SIZE,
        THUMBNAIL_SIZE);
  }

  private boolean hasMissingLocation(ItemSubmission itemSubmission) {
    return itemSubmission.getListings().stream().map(ItemListing::location)
        .anyMatch(location -> location == null || location.isBlank());
  }

  private boolean hasMissingShop(ItemSubmission itemSubmission) {
    return itemSubmission.getListings().stream().map(ItemListing::shop)
        .anyMatch(shop -> shop == null || shop.isBlank());
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
      return LocalizationUtil.get("infoScreenshotListingsRead")
          .formatted(commoditySubmission.getListings().size());
    }

    if (submission instanceof ItemSubmission itemSubmission) {
      return LocalizationUtil.get("infoScreenshotListingsRead")
          .formatted(itemSubmission.getListings().size());
    }

    return null;
  }

  private String computeId(ScreenshotType type, BufferedImage image) {
    return HashUtil.hash(type.name() + ":" + HashUtil.hash(image));
  }
}
