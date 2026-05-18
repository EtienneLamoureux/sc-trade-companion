package tools.sctrade.companion.domain;

import java.awt.image.BufferedImage;

public interface SubmissionFactory<T> {
  T build(BufferedImage screenCapture);
}
