package tools.sctrade.companion.domain.ocr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.utils.ProcessRunner;

public class WindowsOcr extends Ocr {
  private final Logger logger = LoggerFactory.getLogger(WindowsOcr.class);
  private final DiskImageWriter diskImageWriter;
  private final ProcessRunner processRunner;
  private final ObjectMapper objectMapper;

  public WindowsOcr(List<ImageManipulation> preprocessingManipulations,
      DiskImageWriter diskImageWriter, ProcessRunner processRunner, ObjectMapper objectMapper) {
    super(preprocessingManipulations);
    this.diskImageWriter = diskImageWriter;
    this.processRunner = processRunner;
    this.objectMapper = objectMapper;
  }

  @Override
  protected OcrResult process(BufferedImage image) {
    var path = diskImageWriter.write(image, ImageType.SCREENSHOT).orElseThrow().toAbsolutePath()
        .toString();

    // Command to run the windows-ocr-wrapper executable
    // Assumes the wrapper is built and located at the specified relative path
    var command = List.of("bin/windowsocr/windows-ocr-wrapper.exe", path);

    var outputLines = processRunner.runNoFail(command);

    // Join lines back into a single string for JSON parsing (in case of multiline output, though
    // wrapper output is single line usually)
    var jsonOutput = String.join("", outputLines);

    if (jsonOutput.isBlank()) {
      logger.warn("WindowsOCR returned empty output.");
      return new OcrResult(List.of());
    }

    try {
      List<WordData> words =
          objectMapper.readValue(jsonOutput, new TypeReference<List<WordData>>() {});

      var locatedWords = words.stream().map(this::toLocatedWord).collect(Collectors.toList());

      return new OcrResult(locatedWords);

    } catch (JsonProcessingException e) {
      logger.error("Failed to parse WindowsOCR output: {}", jsonOutput, e);
      return new OcrResult(List.of());
    }
  }

  private LocatedWord toLocatedWord(WordData wordData) {
    var rect = new Rectangle((int) wordData.X, (int) wordData.Y, (int) wordData.Width,
        (int) wordData.Height);
    return new LocatedWord(wordData.Text.toLowerCase(), rect);
  }

  // Helper class for JSON mapping
  private static class WordData {
    public String Text;
    public double X;
    public double Y;
    public double Width;
    public double Height;
  }
}
