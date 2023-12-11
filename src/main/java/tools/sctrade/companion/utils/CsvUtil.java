package tools.sctrade.companion.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import tools.sctrade.companion.exceptions.CsvParsingException;

/**
 * Encapsulates OpenCSV.
 *
 * @see <a href="https://www.baeldung.com/opencsv">Reference</a>
 */
public class CsvUtil {
  private CsvUtil() {}

  public static void write(Path path, Collection<List<String>> lines) throws IOException {
    write(path, lines, true);
  }

  public static void write(Path path, Collection<List<String>> lines, boolean apprendLines)
      throws IOException {
    Files.createDirectories(path.getParent());

    try (CSVWriter writer = new CSVWriter(new FileWriter(path.toString(), apprendLines))) {
      for (List<String> line : lines) {
        writer.writeNext(line.toArray(new String[0]));
      }
    } catch (IOException e) {
      throw new CsvParsingException(e);
    }
  }

  public static Collection<List<String>> read(Path path, boolean hasHeader) {
    try (Reader reader = new FileReader(path.toString())) {
      try (CSVReader csvReader =
          new CSVReaderBuilder(reader).withSkipLines(hasHeader ? 1 : 0).build()) {
        List<String[]> lines = csvReader.readAll();

        Collection<List<String>> formattedLines = new ConcurrentLinkedQueue<>();
        lines.parallelStream().forEach(n -> formattedLines.add(Arrays.asList(n)));

        return formattedLines;
      }
    } catch (IOException | CsvException e) {
      throw new CsvParsingException(e);
    }
  }
}
