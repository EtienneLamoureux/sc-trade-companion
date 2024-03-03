package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.utils.ImageUtil;

public class TransactionTypeExtractor {
  private final Logger logger = LoggerFactory.getLogger(TransactionTypeExtractor.class);
  private static final String SHOP_INVENTORY = "shop inventory";

  private ImageWriter imageWriter;

  public TransactionTypeExtractor(ImageWriter imageWriter) {
    this.imageWriter = imageWriter;
  }

  public TransactionType extract(BufferedImage screenCapture, OcrResult result) {
    screenCapture = cropAroundButtons(screenCapture, result);
    var boundingBoxes = getButtonBoundingBoxes(screenCapture);

    boundingBoxes
        .sort((n, m) -> Double.compare(m.getWidth() * m.getHeight(), n.getWidth() * n.getHeight()));
    var boundingBoxesOrderedByX = boundingBoxes.subList(0, 2);
    boundingBoxesOrderedByX.sort((n, m) -> Double.compare(n.getMinX(), m.getMinX()));

    Rectangle buyRectangle = boundingBoxesOrderedByX.get(0);
    var buyImage = ImageUtil.crop(screenCapture, buyRectangle);
    imageWriter.write(buyImage, ImageType.BUY_BUTTON);

    Rectangle sellRectangle = boundingBoxesOrderedByX.get(1);
    var sellImage = ImageUtil.crop(screenCapture, sellRectangle);
    imageWriter.write(sellImage, ImageType.SELL_BUTTON);

    double buyButtonAreaOverSellButtonArea = (buyRectangle.getWidth() * buyRectangle.getHeight())
        / (sellRectangle.getWidth() * sellRectangle.getHeight());

    if (Math.abs(1 - buyButtonAreaOverSellButtonArea) >= 0.2) {
      logger.warn("Detected buttons are not the same size: one button may have not been captured");
      return (buyButtonAreaOverSellButtonArea > 1) ? TransactionType.SELLS : TransactionType.BUYS;
    } else {
      return extractByLuminance(buyImage, sellImage);
    }
  }

  private BufferedImage cropAroundButtons(BufferedImage screenCapture, OcrResult result) {
    Rectangle shopInventoryRectangle = getShopInventoryRectangle(result);
    screenCapture = ImageUtil.crop(screenCapture, new Rectangle((screenCapture.getWidth() / 2), 0,
        (screenCapture.getWidth() - (screenCapture.getWidth() / 2)), screenCapture.getHeight()));
    Rectangle buttonsAreaRectangle = new Rectangle((int) shopInventoryRectangle.getMinX(),
        (int) (shopInventoryRectangle.getMinY() - shopInventoryRectangle.getHeight()),
        (int) (shopInventoryRectangle.getWidth() * 3),
        (int) (shopInventoryRectangle.getHeight() * 5));
    screenCapture = ImageUtil.crop(screenCapture, buttonsAreaRectangle);
    screenCapture = ImageUtil.makeGreyscaleCopy(screenCapture);

    return screenCapture;
  }

  private List<Rectangle> getButtonBoundingBoxes(BufferedImage screenCapture) {
    var image = ImageUtil.makeCopy(screenCapture);
    image = ImageUtil.applyOtsuBinarization(image);
    imageWriter.write(image, ImageType.BUTTONS);
    var boundingBoxes = ImageUtil.findBoundingBoxes(image);

    return boundingBoxes;
  }

  private TransactionType extractByLuminance(BufferedImage buyImage, BufferedImage sellImage) {
    var buyRectangleColor = ImageUtil.calculateDominantColor(buyImage);
    var buyRectangleLuminance = buyRectangleColor.getRed();

    var sellRectangleColor = ImageUtil.calculateDominantColor(sellImage);
    var sellRectangleLuminance = sellRectangleColor.getRed();

    return (buyRectangleLuminance > sellRectangleLuminance) ? TransactionType.SELLS
        : TransactionType.BUYS;
  }

  private Rectangle getShopInventoryRectangle(OcrResult result) {
    var fragments = result.getColumns().parallelStream()
        .flatMap(n -> n.getFragments().parallelStream()).toList();
    LocatedFragment shopInventoryFragment =
        OcrUtil.findFragmentClosestTo(fragments, SHOP_INVENTORY);

    return shopInventoryFragment.getBoundingBox();
  }
}
