package tools.sctrade.companion.domain.image;

import java.awt.image.BufferedImage;

public interface Ocr {
  String read(BufferedImage image);
}
