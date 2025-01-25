package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.exceptions.LocationNotFoundException;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.utils.StringUtil;

/**
 * Reads the location of a commodity kiosk from the left side of a screen capture.
 */
public class CommodityLocationReader {
  private static final String YOUR_INVENTORIES = "your inventories";

  private final Logger logger = LoggerFactory.getLogger(CommodityLocationReader.class);

  private LocationRepository locationRepository;
  private ThreadLocal<Ocr> locationOcr;

  /**
   * Creates a new instance of the commodity location reader.
   *
   * @param preprocessingManipulations the preprocessing manipulations to apply to the screen
   *        capture, in order
   * @param locationRepository the location repository, used to spell check the read location
   */
  public CommodityLocationReader(List<ImageManipulation> preprocessingManipulations,
      LocationRepository locationRepository) {
    this.locationRepository = locationRepository;

    this.locationOcr = ThreadLocal
        .withInitial(() -> new CommodityLocationTesseractOcr(preprocessingManipulations));
  }

  /**
   * Reads the location of a commodity kiosk from a screen capture.
   *
   * @param screenCapture the screen capture
   * @return the location of the commodity kiosk, if found
   */
  public Optional<String> read(BufferedImage screenCapture) {
    try {
      logger.debug("Reading location...");
      OcrResult locationResult = locationOcr.get().read(screenCapture);
      var location = extractLocation(locationResult);
      logger.debug("Read location '{}'", location);

      return location;
    } catch (Exception e) {
      logger.error("Error while reading location", e);

      return Optional.empty();
    } finally {
      locationOcr.remove();
    }
  }

  private Optional<String> extractLocation(OcrResult result) {
    List<LocatedFragment> fragments =
        result.getColumns().stream().flatMap(n -> n.getFragments().stream()).toList();
    var yourInventoriesFragment = OcrUtil.findFragmentClosestTo(fragments, YOUR_INVENTORIES);

    // Return the fragment that follows "your inventories"
    var it = fragments.iterator();

    while (it.hasNext()) {
      var next = it.next();

      if (next.equals(yourInventoriesFragment)) {
        String rawLocation = it.next().getText();
        logger.debug("Read raw location '{}'", rawLocation);

        try {
          String spellCheckedLocation =
              StringUtil.spellCheck(rawLocation, locationRepository.findAllLocations());
          return Optional.of(spellCheckedLocation);
        } catch (NoCloseStringException e) {
          logger.warn("Could not spell-check location '{}'", rawLocation);
          return Optional.empty();
        } catch (NoSuchElementException e) {
          throw new LocationNotFoundException(fragments);
        }
      }
    }

    throw new LocationNotFoundException(fragments);
  }
}
