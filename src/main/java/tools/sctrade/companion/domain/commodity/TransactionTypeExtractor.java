package tools.sctrade.companion.domain.commodity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.ocr.LocatedFragment;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.ocr.OcrUtil;
import tools.sctrade.companion.utils.ImageUtil;

public class TransactionTypeExtractor {
  private static final String SHOP_INVENTORY = "shop inventory";

  private ImageWriter imageWriter;

  public TransactionTypeExtractor(ImageWriter imageWriter) {
    this.imageWriter = imageWriter;
  }

  public TransactionType extract(BufferedImage screenCapture, OcrResult result) {
    Rectangle shopInv = getShopInventoryRectangle(result);
    screenCapture = ImageUtil.crop(screenCapture, new Rectangle((screenCapture.getWidth() / 2), 0,
        (screenCapture.getWidth() - (screenCapture.getWidth() / 2)), screenCapture.getHeight()));
    screenCapture = ImageUtil.crop(screenCapture,
        new Rectangle((int) shopInv.getMinX(), (int) (shopInv.getMinY() - shopInv.getHeight()),
            (int) (shopInv.getWidth() * 3), (int) (shopInv.getHeight() * 4)));
    screenCapture = ImageUtil.makeGreyscaleCopy(screenCapture);

    var image = ImageUtil.makeCopy(screenCapture);
    image = ImageUtil.applyOtsuBinarization(image);
    imageWriter.write(image, ImageType.BUTTONS);
    var boundingBoxes = ImageUtil.findBoundingBoxes(image);

    boundingBoxes
        .sort((n, m) -> Double.compare(m.getWidth() * m.getHeight(), n.getWidth() * n.getHeight()));
    var boundingBoxesOrderedByX = boundingBoxes.subList(0, 2);
    boundingBoxesOrderedByX.sort((n, m) -> Double.compare(n.getMinX(), m.getMinX()));
    Rectangle buyRectangle = boundingBoxesOrderedByX.get(0);
    Rectangle sellRectangle = boundingBoxesOrderedByX.get(1);

    var buyImage = ImageUtil.crop(screenCapture, buyRectangle);
    var buyRectangleColor = ImageUtil.calculateAverageColor(buyImage);
    var buyRectangleLuminance = buyRectangleColor.getRed();
    imageWriter.write(buyImage, ImageType.BUY_BUTTON);

    var sellImage = ImageUtil.crop(screenCapture, sellRectangle);
    var sellRectangleColor = ImageUtil.calculateAverageColor(sellImage);
    var sellRectangleLuminance = sellRectangleColor.getRed();
    imageWriter.write(sellImage, ImageType.SELL_BUTTON);

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
