package tools.sctrade.companion.domain.item;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import tools.sctrade.companion.domain.LocationReader;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.exceptions.LocationNotFoundException;
import tools.sctrade.companion.exceptions.NoCloseStringException;

public class ItemShopReader extends LocationReader {

  private static final String WALLET = "wallet:";
  private static final String SELL = "sell";
  private static final String COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS =
      "Could not find '{}' fragment. Falling back to default bounds";

  public ItemShopReader() {
    super(null);
  }

  @Override
  protected OcrResult crop(BufferedImage screenCapture, OcrResult ocrResult) {
    return ocrResult.crop(calculateShopBoundingBox(ocrResult));
  }

  @Override
  protected String getLocationLabel() {
    return SELL;
  }

  @Override
  protected String findRawLocation(List<LocatedFragment> fragments) {
    if (fragments.isEmpty()) {
      throw new LocationNotFoundException(fragments);
    }

    String shopText = fragments.stream().map(LocatedFragment::getText)
        .reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b).trim();
    logger.debug("Read raw shop text: '{}'", shopText);

    return shopText;
  }

  @Override
  protected Optional<String> spellCheckLocation(String rawLocation) {
    // No shop repository available; return raw value as-is
    return Optional.of(rawLocation);
  }

  private Rectangle calculateShopBoundingBox(OcrResult ocrResult) {
    var sellFragment = findFragment(ocrResult, SELL);
    var chooseDestinationFragment = findFragment(ocrResult, ItemLocationReader.CHOOSE_DESTINATION);
    var walletFragment = findWalletFragment(ocrResult);

    int minX;
    int maxX;
    int minY = (int) ocrResult.getBoundingBox().getMinY();
    int maxY;

    if (sellFragment.isPresent()) {
      minX = (int) sellFragment.get().getBoundingBox().getMaxX();
    } else {
      logger.warn(COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS, SELL);
      minX = (int) (ocrResult.getBoundingBox().getWidth() * 0.33);
    }

    if (walletFragment.isPresent()) {
      maxX = (int) walletFragment.get().getBoundingBox().getMinX();
    } else {
      logger.warn(COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS, WALLET);
      maxX = (int) (ocrResult.getBoundingBox().getWidth() * 0.66);
    }

    if (chooseDestinationFragment.isPresent()) {
      maxY = (int) chooseDestinationFragment.get().getBoundingBox().getMinY();
    } else {
      logger.warn(COULD_NOT_FIND_FRAGMENT_FALLING_BACK_TO_DEFAULT_BOUNDS,
          ItemLocationReader.CHOOSE_DESTINATION);
      maxY = (int) ocrResult.getBoundingBox().getHeight() / 4;
    }

    int width = maxX - minX;
    int height = maxY - minY;

    return new Rectangle(minX, minY, width, height);
  }

  private Optional<LocatedFragment> findFragment(OcrResult ocrResult, String fragment) {
    try {
      return Optional.of(OcrUtil.findFragmentClosestTo(ocrResult, fragment));
    } catch (NoCloseStringException e) {
      logger.warn("Could not find '{}' fragment", fragment);
      return Optional.empty();
    }
  }

  private Optional<LocatedFragment> findWalletFragment(OcrResult ocrResult) {
    return ocrResult.getFragments().parallelStream()
        .filter(n -> n.getText().trim().startsWith(WALLET)).findFirst();
  }
}
