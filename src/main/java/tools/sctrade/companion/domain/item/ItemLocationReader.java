package tools.sctrade.companion.domain.item;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import tools.sctrade.companion.domain.LocationReader;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.exceptions.LocationNotFoundException;

public class ItemLocationReader extends LocationReader {

  static final String CHOOSE_DESTINATION = "choose destination";

  /**
   * Creates a new instance of the item location reader.
   *
   * @param locationRepository the location repository, used to spell check the read location
   */
  public ItemLocationReader(LocationRepository locationRepository) {
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
    return CHOOSE_DESTINATION;
  }

  @Override
  protected String findRawLocation(List<LocatedFragment> fragments) {
    var yourInventoriesFragment = OcrUtil.findFragmentClosestTo(fragments, getLocationLabel());

    // Return the fragment that follows "your inventories"
    var it = fragments.iterator();

    while (it.hasNext()) {
      var next = it.next();

      if (next.equals(yourInventoriesFragment)) {
        String rawLocation = it.next().getText();
        logger.debug("Read raw location '{}'", rawLocation);
        return rawLocation;
      }
    }

    throw new LocationNotFoundException(fragments);
  }
}
