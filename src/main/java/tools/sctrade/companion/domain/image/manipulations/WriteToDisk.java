package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;

/**
 * Writes an image to disk. Mostly used to debug by inserting between pre-processing steps.
 */
public class WriteToDisk implements ImageManipulation {
  private ImageWriter<Optional<Path>> imageWriter;

  public WriteToDisk(ImageWriter<Optional<Path>> imageWriter) {
    this.imageWriter = imageWriter;
  }

  @Override
  public BufferedImage manipulate(BufferedImage image) {
    imageWriter.write(image, ImageType.PREPROCESSED);

    return image;
  }

}
