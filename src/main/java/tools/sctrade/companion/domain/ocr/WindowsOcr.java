package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.utils.JsonUtil;
import tools.sctrade.companion.utils.LocalizationUtil;
import tools.sctrade.companion.utils.ProcessRunner;

public class WindowsOcr extends Ocr {
  private final Logger logger = LoggerFactory.getLogger(WindowsOcr.class);

  private final DiskImageWriter diskImageWriter;
  private final ProcessRunner processRunner;
  private final NotificationService notificationService;

  public WindowsOcr(List<ImageManipulation> preprocessingManipulations,
      DiskImageWriter diskImageWriter, ProcessRunner processRunner,
      NotificationService notificationService) {
    super(preprocessingManipulations);

    this.diskImageWriter = diskImageWriter;
    this.processRunner = processRunner;
    this.notificationService = notificationService;
  }

  @Override
  protected OcrResult process(BufferedImage image) {
    var imagePath = diskImageWriter.write(image, ImageType.SCREENSHOT).orElseThrow()
        .toAbsolutePath().toString();

    var command = List.of("bin/windowsocr/windows-ocr-wrapper.exe", imagePath);
    var output = processRunner.runNoFail(command);

    var locatedWords = parseOutput(output);

    return new OcrResult(locatedWords);
  }

  private List<LocatedWord> parseOutput(List<String> output) {
    var json = String.join("", output);

    if (json.isBlank()) {
      logger.warn("WindowsOCR returned empty output");
      return Collections.emptyList();
    }

    try {
      var words = JsonUtil.parseList(json, WindowsOcrLocatedWord.class);

      return words.stream().map(n -> toLocatedWord(n)).toList();
    } catch (Exception e) {
      logger.error("Could not parse Windows OCR output", e);
      notificationService.error(LocalizationUtil.get("errorParsingOcrOutput"));
      return Collections.emptyList();
    }
  }

  private LocatedWord toLocatedWord(WindowsOcrLocatedWord wordData) {
    var rectangle = new Rectangle((int) wordData.X, (int) wordData.Y, (int) wordData.Width,
        (int) wordData.Height);

    return new LocatedWord(wordData.Text.toLowerCase(), rectangle);
  }

  private static record WindowsOcrLocatedWord(String Text, double X, double Y, double Width,
      double Height) {
  }
}
