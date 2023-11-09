package tools.sctrade.companion.domain.image.manipulations;

import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.image.ImageWriter;

public class WriteToDisk implements ImageManipulation {
  private final Logger logger = LoggerFactory.getLogger(WriteToDisk.class);
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
