package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.exceptions.UnreadableTransactionTypeException;
import tools.sctrade.companion.utils.ImageUtil;

public class TransactionTypeExtractor {
  private final Logger logger = LoggerFactory.getLogger(TransactionTypeExtractor.class);
  static final String SHOP_INVENTORY = "shop inventory";
  private static final String BUY = "buy";
  private static final Object SELL = "local market value";

  private ImageWriter imageWriter;

  public TransactionTypeExtractor(ImageWriter imageWriter) {
    this.imageWriter = imageWriter;
  }

  public TransactionType extract(BufferedImage screenCapture, OcrResult result) {
    screenCapture = ImageUtil.crop(screenCapture, new Rectangle((screenCapture.getWidth() / 2), 0,
        (screenCapture.getWidth() - (screenCapture.getWidth() / 2)), screenCapture.getHeight()));
    screenCapture = ImageUtil.makeGreyscaleCopy(screenCapture);
    var buttonAreaBoundingBox = findButtonAreaBoundingBox(screenCapture, result);
    screenCapture = ImageUtil.crop(screenCapture, buttonAreaBoundingBox);
    var boundingBoxes = getButtonBoundingBoxes(screenCapture);

    boundingBoxes
        .sort((n, m) -> Double.compare(m.getWidth() * m.getHeight(), n.getWidth() * n.getHeight()));
    var boundingBoxesOrderedByX = new ArrayList<>(boundingBoxes.subList(0, 2));
    boundingBoxesOrderedByX.sort((n, m) -> Double.compare(n.getMinX(), m.getMinX()));

    Rectangle buyRectangle = boundingBoxesOrderedByX.get(0);
    var buyImage = ImageUtil.crop(screenCapture, buyRectangle);
    imageWriter.write(buyImage, ImageType.BUY_BUTTON);

    Rectangle sellRectangle = boundingBoxesOrderedByX.get(1);
    var sellImage = ImageUtil.crop(screenCapture, sellRectangle);
    imageWriter.write(sellImage, ImageType.SELL_BUTTON);

    if (buttonsAreAligned(buyRectangle, sellRectangle)) {
      return extractByLuminance(buyImage, sellImage);
    } else {
      logger.warn("Detected buttons are not aligned: one button may have not been captured");
      var buttonBoundingBox = getNormalizedButtonBoundingBox(buttonAreaBoundingBox, boundingBoxes);

      return extractByStringPosition(result, buttonBoundingBox);
    }
  }

  private Rectangle findButtonAreaBoundingBox(BufferedImage screenCapture, OcrResult result) {
    Rectangle shopInventoryRectangle = OcrUtil.getRectangleClosestTo(result, SHOP_INVENTORY);
    Rectangle buttonAreaBoundingBox = new Rectangle((int) shopInventoryRectangle.getMinX(),
        (int) (shopInventoryRectangle.getMinY() - shopInventoryRectangle.getHeight()),
        (int) (shopInventoryRectangle.getWidth() * 3),
        (int) (shopInventoryRectangle.getHeight() * 5));

    return buttonAreaBoundingBox;
  }

  private List<Rectangle> getButtonBoundingBoxes(BufferedImage screenCapture) {
    var image = ImageUtil.makeCopy(screenCapture);
    image = ImageUtil.applyOtsuBinarization(image);
    imageWriter.write(image, ImageType.BUTTONS);
    var boundingBoxes = ImageUtil.findBoundingBoxes(image);

    return boundingBoxes;
  }

  private boolean buttonsAreAligned(Rectangle buyRectangle, Rectangle sellRectangle) {
    var yOverlap1 = sellRectangle.getMaxY() - buyRectangle.getMinY();
    var yOverlap2 = buyRectangle.getMaxY() - sellRectangle.getMinY();
    var yOverlap = Math.min(yOverlap1, yOverlap2);
    var yHeight = Math.min(buyRectangle.getHeight(), sellRectangle.getHeight());

    return (yOverlap / yHeight) > 0.66;
  }

  private Rectangle getNormalizedButtonBoundingBox(Rectangle buttonAreaBoundingBox,
      List<Rectangle> boundingBoxes) {
    var buttonBoundingBox = boundingBoxes.iterator().next();
    buttonBoundingBox.setLocation(buttonAreaBoundingBox.x + buttonBoundingBox.x,
        buttonAreaBoundingBox.y + buttonBoundingBox.y);

    return buttonBoundingBox;
  }

  private TransactionType extractByStringPosition(OcrResult result, Rectangle buttonBoundingBox) {
    var buttonFragments =
        result.getColumns().parallelStream().flatMap(n -> n.getFragments().stream())
            .filter(n -> n.getBoundingBox().intersectsLine(Double.MIN_VALUE,
                buttonBoundingBox.getCenterY(), Double.MAX_VALUE, buttonBoundingBox.getCenterY()))
            .toList();

    var buyFragment =
        buttonFragments.parallelStream().filter(n -> n.getText().equals(BUY)).findFirst();

    if (buyFragment.isPresent()) {
      if (buttonBoundingBox.contains(buyFragment.get().getBoundingBox())) {
        return TransactionType.SELLS;
      }

      if (buyFragment.get().getBoundingBox().getMaxX() < buttonBoundingBox.getMinX()) {
        // buttonBoundingBox is the SELL button
        return TransactionType.BUYS;
      }
    }

    var sellFragment =
        buttonFragments.parallelStream().filter(n -> n.getText().equals(SELL)).findFirst();

    if (sellFragment.isPresent()) {
      if (buttonBoundingBox.contains(sellFragment.get().getBoundingBox())) {
        return TransactionType.BUYS;
      }

      if (buttonBoundingBox.getMaxX() < sellFragment.get().getBoundingBox().getMinX()) {
        // buttonBoundingBox is the BUY button
        return TransactionType.SELLS;
      }
    }

    logger.warn("Could not detect transaction type");
    throw new UnreadableTransactionTypeException();
  }

  private TransactionType extractByLuminance(BufferedImage buyImage, BufferedImage sellImage) {
    var buyRectangleColor = ImageUtil.calculateDominantColor(buyImage);
    var buyRectangleLuminance = buyRectangleColor.getRed();

    var sellRectangleColor = ImageUtil.calculateDominantColor(sellImage);
    var sellRectangleLuminance = sellRectangleColor.getRed();

    return (buyRectangleLuminance > sellRectangleLuminance) ? TransactionType.SELLS
        : TransactionType.BUYS;
  }
}
