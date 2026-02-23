package tools.sctrade.companion.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRunner {
  private final Logger logger = LoggerFactory.getLogger(ProcessRunner.class);

  public ProcessRunner() {}

  public List<String> runNoFail(List<String> command) {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    File directory = new File(command.get(0).substring(0, command.get(0).lastIndexOf("/")));
    processBuilder.directory(directory);
    processBuilder.redirectErrorStream(true);

    try {
      Process process = processBuilder.start();

      var lines = aggregateOutput(process);
      wait(process);

      return lines;
    } catch (IOException | InterruptedException e) {
      logger.error(e.getMessage());

      return Collections.emptyList();
    }
  }

  private ArrayList<String> aggregateOutput(Process process) throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      var lines = new ArrayList<String>();
      String line;

      while ((line = reader.readLine()) != null) {
        lines.add(line);
        logger.debug(line);
      }

      return lines;
    }
  }

  private void wait(Process process) throws InterruptedException {
    int exitCode = process.waitFor();
    logger.debug("PaddleOCR exited with code : {}", exitCode);
  }
}
