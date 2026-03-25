package tools.sctrade.companion.domain.ocr;

import com.google.protobuf.CodedInputStream;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the Chrome Screen AI protobuf response. Extracts word-level OCR results from the
 * {@code VisualAnnotation} message returned by {@code PerformOCR}.
 *
 * <p>
 * The protobuf schema (from {@code chrome_screen_ai.proto}):
 *
 * <pre>
 * message VisualAnnotation { repeated LineBox lines = 2; }
 * message LineBox { repeated WordBox words = 1; Rect bounding_box = 2; string utf8_string = 3; }
 * message WordBox { Rect bounding_box = 2; string utf8_string = 3; }
 * message Rect { int32 x = 1; int32 y = 2; int32 width = 3; int32 height = 4; float angle = 5; }
 * </pre>
 */
public class ScreenAiResponse {
  private ScreenAiResponse() {
    // Utility class
  }

  /**
   * Parses a {@code VisualAnnotation} protobuf message and extracts all recognised words with their
   * bounding boxes.
   *
   * @param data the raw protobuf bytes returned by {@code PerformOCR}
   * @return the list of located words
   * @throws IOException if the protobuf data is malformed
   */
  public static List<LocatedWord> parseWords(byte[] data) throws IOException {
    var words = new ArrayList<LocatedWord>();
    var input = CodedInputStream.newInstance(data);

    while (!input.isAtEnd()) {
      int tag = input.readTag();
      int fieldNumber = tag >>> 3;

      if (fieldNumber == 2) {
        // VisualAnnotation.lines (repeated LineBox)
        parseLineBox(input.readByteArray(), words);
      } else {
        input.skipField(tag);
      }
    }

    return words;
  }

  // -------------------------------------------------------------------------
  // Protobuf parsing helpers
  // -------------------------------------------------------------------------

  private static void parseLineBox(byte[] data, List<LocatedWord> words) throws IOException {
    var input = CodedInputStream.newInstance(data);

    while (!input.isAtEnd()) {
      int tag = input.readTag();
      int fieldNumber = tag >>> 3;

      if (fieldNumber == 1) {
        // LineBox.words (repeated WordBox)
        parseWordBox(input.readByteArray(), words);
      } else {
        input.skipField(tag);
      }
    }
  }

  private static void parseWordBox(byte[] data, List<LocatedWord> words) throws IOException {
    var input = CodedInputStream.newInstance(data);
    String text = "";
    Rectangle boundingBox = new Rectangle();

    while (!input.isAtEnd()) {
      int tag = input.readTag();
      int fieldNumber = tag >>> 3;

      switch (fieldNumber) {
        case 2:
          // WordBox.bounding_box (Rect)
          boundingBox = parseRect(input.readByteArray());
          break;
        case 3:
          // WordBox.utf8_string (string)
          text = input.readStringRequireUtf8();
          break;
        default:
          input.skipField(tag);
          break;
      }
    }

    if (!text.isEmpty()) {
      words.add(new LocatedWord(text.toLowerCase(), boundingBox));
    }
  }

  private static Rectangle parseRect(byte[] data) throws IOException {
    var input = CodedInputStream.newInstance(data);
    int x = 0;
    int y = 0;
    int width = 0;
    int height = 0;

    while (!input.isAtEnd()) {
      int tag = input.readTag();
      int fieldNumber = tag >>> 3;

      switch (fieldNumber) {
        case 1:
          x = input.readInt32();
          break;
        case 2:
          y = input.readInt32();
          break;
        case 3:
          width = input.readInt32();
          break;
        case 4:
          height = input.readInt32();
          break;
        default:
          input.skipField(tag);
          break;
      }
    }

    return new Rectangle(x, y, width, height);
  }
}
