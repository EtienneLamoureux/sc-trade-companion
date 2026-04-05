package tools.sctrade.companion.domain;

import java.awt.image.BufferedImage;
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
   * @param screenCapture the original screen capture
   * @param ocrResult the full OCR result
   * @return the location, if found
   */
  public Optional<String> read(BufferedImage screenCapture, OcrResult ocrResult) {
    return read(crop(screenCapture, ocrResult));
  }

  /**
   * Reads the location from a cropped OCR result.
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

  /**
   * Crops the full OCR result to the region relevant for location reading.
   *
   * @param screenCapture the original screen capture
   * @param ocrResult the full OCR result
   * @return the cropped OCR result
   */
  protected abstract OcrResult crop(BufferedImage screenCapture, OcrResult ocrResult);

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
}
