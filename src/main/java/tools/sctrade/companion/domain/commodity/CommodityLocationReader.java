package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import tools.sctrade.companion.domain.LocationReader;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.exceptions.LocationNotFoundException;

/**
 * Reads the location of a commodity kiosk from the left side of a screen capture.
 */
public class CommodityLocationReader extends LocationReader {
  private static final String YOUR_INVENTORIES = "your inventories";

  /**
   * Creates a new instance of the commodity location reader.
   *
   * @param locationRepository the location repository, used to spell check the read location
   */
  public CommodityLocationReader(LocationRepository locationRepository) {
    super(locationRepository);
  }

  @Override
  protected OcrResult crop(BufferedImage screenCapture, OcrResult ocrResult) {
    var boundingBox =
        new Rectangle(0, 0, screenCapture.getWidth() / 2, screenCapture.getHeight() / 3);
    return ocrResult.crop(boundingBox);
  }

  @Override
  protected String getLocationLabel() {
    return YOUR_INVENTORIES;
  }

  @Override
  protected String findRawLocation(List<LocatedFragment> fragments) {
    var yourInventoriesFragment = OcrUtil.findFragmentClosestTo(fragments, getLocationLabel());
    var anchorBox = yourInventoriesFragment.getBoundingBox();
    int anchorMaxY = anchorBox.y + anchorBox.height;
    int anchorMinX = anchorBox.x;
    int anchorMaxX = anchorBox.x + anchorBox.width;

    // Find the fragment that:
    // 1. overlaps with yourInventoriesFragment on the X axis
    // 2. starts below yourInventoriesFragment (minY > anchorMaxY)
    // 3. has the smallest vertical distance from yourInventoriesFragment among all such fragments
    LocatedFragment rawLocationFragment =
        fragments.stream().filter(f -> !f.equals(yourInventoriesFragment)).filter(f -> {
          var box = f.getBoundingBox();
          int fMinX = box.x;
          int fMaxX = box.x + box.width;
          return fMinX < anchorMaxX && fMaxX > anchorMinX;
        }).filter(f -> f.getBoundingBox().y > anchorMaxY)
            .min(java.util.Comparator.comparingInt(f -> f.getBoundingBox().y - anchorMaxY))
            .orElseThrow(() -> new LocationNotFoundException(fragments));

    String rawLocation = rawLocationFragment.getText();
    logger.debug("Read raw location '{}'", rawLocation);
    return rawLocation;
  }

}
