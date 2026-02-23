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
  private static final String DOT_NET = ".NET";

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

    var command = List.of("bin/oneocr-wrapper/OneOcrWrapper.exe", imagePath);
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
      var result = JsonUtil.parse(json, WindowsOcrResult.class);

      return result.lines().stream().map(n -> toLocatedWord(n)).toList();
    } catch (Exception e) {
      logger.error("Could not parse Windows OCR output", e);

      if (json.contains(DOT_NET)) {
        notificationService.error(LocalizationUtil.get("errorMissingDotNet"));
      }

      return Collections.emptyList();
    }
  }

  private LocatedWord toLocatedWord(WindowsOcrLocatedLine line) {
    var rectangle = new Rectangle(line.boundingBox().x(), line.boundingBox().y(),
        line.boundingBox().width(), line.boundingBox().height());

    return new LocatedWord(line.text().toLowerCase(), rectangle);
  }

  private static record WindowsOcrResult(List<WindowsOcrLocatedLine> lines) {
  }

  private static record WindowsOcrLocatedLine(String text, BoundingBox boundingBox,
      List<WindowsOcrLocatedWord> words) {
  }

  private static record WindowsOcrLocatedWord(String text, BoundingBox boundingBox) {
  }

  private static record BoundingBox(int x, int y, int width, int height) {
  }
}
