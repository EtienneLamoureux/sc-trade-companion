package tools.sctrade.companion.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ResourceUtilTest {

  @Test
  void givenVideoResourceWhenCopiedToTempFileThenTempFileExists() throws Exception {
    Path tempFile = ResourceUtil.copyResourceToTempFile("/videos/example-kiosk-commodity.mp4");

    assertTrue(Files.exists(tempFile));
  }
}
