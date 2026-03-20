package tools.sctrade.companion.domain;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.exceptions.LocationNotFoundException;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.utils.StringUtil;

/**
 * Reads a location from OCR results using a template method pattern.
 */
public abstract class LocationReader {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private LocationRepository locationRepository;

  protected LocationReader(LocationRepository locationRepository) {
    this.locationRepository = locationRepository;
  }

  /**
   * Reads the location from a screen capture.
   *
   * @param locationResult the cropped OCR results corresponding to the location dropdown
   * @return the location, if found
   */
  public Optional<String> read(OcrResult locationResult) {
    try {
      logger.debug("Reading location...");
      var location = extractLocation(locationResult);
      logger.debug("Read location '{}'", location);

      return location;
    } catch (Exception e) {
      logger.error("Error while reading location", e);

      return Optional.empty();
    }
  }

  private Optional<String> extractLocation(OcrResult result) {
    List<LocatedFragment> fragments =
        result.getLines().stream().flatMap(n -> n.getFragments().stream()).toList();
    String rawLocation = findRawLocation(fragments);

    try {
      return spellCheckLocation(rawLocation);
    } catch (NoSuchElementException e) {
      throw new LocationNotFoundException(fragments);
    }
  }

  protected abstract String getLocationLabel();

  protected abstract String findRawLocation(List<LocatedFragment> fragments);

  protected Optional<String> spellCheckLocation(String rawLocation) {
    try {
      String spellCheckedLocation =
          StringUtil.spellCheck(rawLocation, locationRepository.findAllLocations());
      return Optional.of(spellCheckedLocation);
    } catch (NoCloseStringException e) {
      logger.warn("Could not spell-check location '{}'", rawLocation);
      return Optional.empty();
    }
  }
}
