package tools.sctrade.companion.domain.ocr;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.utils.ProcessRunner;

class PaddleOcrITest {
  private PaddleOcr ocr;

  @BeforeEach
  void setUp() {
    ocr = new PaddleOcr(Collections.emptyList(), new DiskImageWriter(new SettingRepository()),
        new ProcessRunner());
  }

  @Test
  void bonjour() {
    ocr.process(null);
  }
}
