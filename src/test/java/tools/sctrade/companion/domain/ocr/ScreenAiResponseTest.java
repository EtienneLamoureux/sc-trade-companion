package tools.sctrade.companion.domain.ocr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.CodedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScreenAiResponseTest {

  @Test
  void givenEmptyDataWhenParsingThenReturnsEmptyList() throws IOException {
    List<LocatedWord> words = ScreenAiResponse.parseWords(new byte[0]);

    assertTrue(words.isEmpty());
  }

  @Test
  void givenSingleWordWhenParsingThenReturnsWordWithBoundingBox() throws IOException {
    byte[] data = buildVisualAnnotation(buildLineBox(buildWordBox("hello", 10, 20, 100, 30)));

    List<LocatedWord> words = ScreenAiResponse.parseWords(data);

    assertEquals(1, words.size());
    assertEquals("hello", words.get(0).getText());
    assertEquals(10, words.get(0).getBoundingBox().x);
    assertEquals(20, words.get(0).getBoundingBox().y);
    assertEquals(100, words.get(0).getBoundingBox().width);
    assertEquals(30, words.get(0).getBoundingBox().height);
  }

  @Test
  void givenMultipleWordsAcrossLinesWhenParsingThenReturnsAllWords() throws IOException {
    byte[] line1 =
        buildLineBox(buildWordBox("foo", 0, 0, 50, 10), buildWordBox("bar", 60, 0, 50, 10));
    byte[] line2 = buildLineBox(buildWordBox("baz", 0, 20, 50, 10));
    byte[] data = buildVisualAnnotation(line1, line2);

    List<LocatedWord> words = ScreenAiResponse.parseWords(data);

    assertEquals(3, words.size());
    assertEquals("foo", words.get(0).getText());
    assertEquals("bar", words.get(1).getText());
    assertEquals("baz", words.get(2).getText());
  }

  @Test
  void givenWordWithEmptyTextWhenParsingThenSkipsIt() throws IOException {
    byte[] data = buildVisualAnnotation(
        buildLineBox(buildWordBox("", 0, 0, 10, 10), buildWordBox("visible", 20, 0, 50, 10)));

    List<LocatedWord> words = ScreenAiResponse.parseWords(data);

    assertEquals(1, words.size());
    assertEquals("visible", words.get(0).getText());
  }

  @Test
  void givenUpperCaseTextWhenParsingThenReturnsLowerCase() throws IOException {
    byte[] data = buildVisualAnnotation(buildLineBox(buildWordBox("HELLO", 0, 0, 50, 10)));

    List<LocatedWord> words = ScreenAiResponse.parseWords(data);

    assertEquals("hello", words.get(0).getText());
  }

  // -------------------------------------------------------------------------
  // Protobuf builders (wire format)
  // -------------------------------------------------------------------------

  private static byte[] buildRect(int x, int y, int width, int height) throws IOException {
    var buf = new ByteArrayOutputStream();
    var out = CodedOutputStream.newInstance(buf);
    if (x != 0) {
      out.writeInt32(1, x);
    }
    if (y != 0) {
      out.writeInt32(2, y);
    }
    if (width != 0) {
      out.writeInt32(3, width);
    }
    if (height != 0) {
      out.writeInt32(4, height);
    }
    out.flush();
    return buf.toByteArray();
  }

  private static byte[] buildWordBox(String text, int x, int y, int w, int h) throws IOException {
    byte[] rect = buildRect(x, y, w, h);

    var buf = new ByteArrayOutputStream();
    var out = CodedOutputStream.newInstance(buf);
    out.writeByteArray(2, rect); // bounding_box = field 2
    out.writeString(3, text); // utf8_string = field 3
    out.flush();
    return buf.toByteArray();
  }

  private static byte[] buildLineBox(byte[]... wordBoxes) throws IOException {
    var buf = new ByteArrayOutputStream();
    var out = CodedOutputStream.newInstance(buf);
    for (byte[] wordBox : wordBoxes) {
      out.writeByteArray(1, wordBox); // words = field 1
    }
    out.flush();
    return buf.toByteArray();
  }

  private static byte[] buildVisualAnnotation(byte[]... lineBoxes) throws IOException {
    var buf = new ByteArrayOutputStream();
    var out = CodedOutputStream.newInstance(buf);
    for (byte[] lineBox : lineBoxes) {
      out.writeByteArray(2, lineBox); // lines = field 2
    }
    out.flush();
    return buf.toByteArray();
  }
}
