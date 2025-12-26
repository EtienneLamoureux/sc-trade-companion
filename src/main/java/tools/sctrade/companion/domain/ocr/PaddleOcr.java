package tools.sctrade.companion.domain.ocr;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.utils.ProcessRunner;

public class PaddleOcr extends Ocr {
  private static final Pattern RELEVANT_LINE_PATTERN = Pattern.compile(
      "^.+ ppocr INFO: \\[\\[\\[([\\d.]+),\\s*([\\d.]+)\\],\\s*\\[\\s*([\\d.]+),\\s*([\\d.]+)\\],\\s*\\[\\s*([\\d.]+),\\s*([\\d.]+)\\],\\s*\\[\\s*([\\d.]+),\\s*([\\d.]+)\\]\\],\\s*\\('([^']+)',\\s*[\\d.]+\\)\\]$");

  private final Logger logger = LoggerFactory.getLogger(PaddleOcr.class);

  private DiskImageWriter diskImageWriter;
  private ProcessRunner processRunner;

  public PaddleOcr(List<ImageManipulation> preprocessingManipulations,
      DiskImageWriter diskImageWriter, ProcessRunner processRunner) {
    super(preprocessingManipulations);

    this.diskImageWriter = diskImageWriter;
    this.processRunner = processRunner;
  }

  @Override
  protected OcrResult process(BufferedImage image) {
    var path = diskImageWriter.write(image, ImageType.SCREENSHOT).orElseThrow().toAbsolutePath()
        .toString();
    var command = List.of("cmd.exe", "/c", "bin\\paddleocr\\paddleocr.exe", "ocr", "-i", path,
        "--lang", "en", "--ocr_version", "PP-OCRv5", "--use_doc_unwarping", "true",
        "--text_rec_score_thresh", "0.90");

    var outputLines = processRunner.runNoFail(command);
    var locatedWords = outputLines.stream().map(n -> RELEVANT_LINE_PATTERN.matcher(n))
        .filter(n -> n.find()).map(n -> buildLocatedWord(n)).filter(n -> n != null).toList();

    return new OcrResult(locatedWords);
  }

  private LocatedWord buildLocatedWord(Matcher matcher) {
    try {
      String string = matcher.group(9).toLowerCase(Locale.ROOT);

      var x1 = new BigDecimal(matcher.group(1)).intValue();
      var y1 = new BigDecimal(matcher.group(2)).intValue();
      var x3 = new BigDecimal(matcher.group(5)).intValue();
      var y3 = new BigDecimal(matcher.group(6)).intValue();

      var width = x3 - x1;
      var height = y3 - y1;

      return new LocatedWord(string, new Rectangle(x1, y1, width, height));
    } catch (Exception e) {
      logger.error("Could not parse '{}'", matcher.group(0));

      return null;
    }
  }

}
