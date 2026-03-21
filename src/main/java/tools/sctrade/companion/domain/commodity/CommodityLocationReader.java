package tools.sctrade.companion.domain.commodity;

import java.util.List;
import tools.sctrade.companion.domain.LocationReader;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
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
  protected String getLocationLabel() {
    return YOUR_INVENTORIES;
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
