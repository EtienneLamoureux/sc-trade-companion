package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;

/**
 * Writes an image to disk. Mostly used to debug by inserting between pre-processing steps.
 */
public class WriteToDisk implements ImageManipulation {
  private ImageWriter imageWriter;

  public WriteToDisk(ImageWriter imageWriter) {
    this.imageWriter = imageWriter;
  }

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    imageWriter.write(image, ImageType.PREPROCESSED);

    return image;
  }

}
