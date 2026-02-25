package tools.sctrade.companion.domain.ocr;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.output.DiskImageWriter;

/**
 * OCR implementation backed by the native OneOCR library via JNA.
 *
 * The image is first written to disk as JPEG (matching the encoding path used by the C# wrapper)
 * and then reloaded with ICC profile processing disabled, so that the raw pixel values fed to the
 * native pipeline are identical to those produced by {@code System.Drawing.Bitmap}.
 */
public class OneOcr extends Ocr {
  private static final Logger logger = LoggerFactory.getLogger(OneOcr.class);

  // -------------------------------------------------------------------------
  // Constants
  // -------------------------------------------------------------------------
  private static final String WRAPPER_DIR = Paths.get("bin/oneocr").toAbsolutePath().toString();
  private static final String DLL_PATH = WRAPPER_DIR + "/oneocr";
  private static final String MODEL_PATH = WRAPPER_DIR + "/oneocr.onemodel";
  private static final byte DISABLE_MODEL_DELAY_LOAD = 0;
  private static final long MAX_RECOGNITION_LINES = 1000;
  /** Decryption key for the bundled OneOCR model file. */
  private static final String MODEL_KEY = "kj)TGtrK>f]b[Piow.gU+nC@s\"\"\"\"\"\"4";

  // -------------------------------------------------------------------------
  // JNA interfaces
  // -------------------------------------------------------------------------

  /** JNA binding for the native {@code oneocr.dll}. */
  interface OneOcrLib extends Library {
    long CreateOcrInitOptions(PointerByReference ctx);

    long OcrInitOptionsSetUseModelDelayLoad(Pointer ctx, byte flag);

    long CreateOcrPipeline(Pointer modelPath, Pointer key, Pointer ctx,
        PointerByReference pipeline);

    long CreateOcrProcessOptions(PointerByReference opt);

    long OcrProcessOptionsSetMaxRecognitionLineCount(Pointer opt, long count);

    long RunOcrPipeline(Pointer pipeline, Img.ByReference img, Pointer opt,
        PointerByReference instance);

    long GetOcrLineCount(Pointer instance, LongByReference count);

    long GetOcrLine(Pointer instance, long index, PointerByReference line);

    long GetOcrLineContent(Pointer line, PointerByReference textPtr);

    long GetOcrLineBoundingBox(Pointer line, PointerByReference boxPtr);

    long GetOcrLineWordCount(Pointer line, LongByReference count);

    long GetOcrWord(Pointer line, long index, PointerByReference word);

    long GetOcrWordContent(Pointer word, PointerByReference textPtr);

    long GetOcrWordBoundingBox(Pointer word, PointerByReference boxPtr);
  }

  /**
   * JNA binding for the subset of {@code kernel32.dll} used to extend the DLL search path, so that
   * {@code oneocr.dll} can resolve its siblings ({@code onnxruntime.dll},
   * {@code opencv_world460.dll}, …).
   */
  private interface Kernel32 extends Library {
    boolean SetDllDirectoryA(String path);
  }

  // -------------------------------------------------------------------------
  // Native structs
  // -------------------------------------------------------------------------

  /**
   * Mirrors the native {@code Img} struct passed to the OCR pipeline.
   *
   * <pre>
   * [StructLayout(LayoutKind.Sequential, Pack = 1)]
   * struct Img { int T; int Col; int Row; int Unk; long Step; long DataPtr; }
   * </pre>
   */
  public static class Img extends Structure {
    /** Image type identifier; {@code 3} denotes a 4-channel BGRA/ARGB image. */
    public int t;
    /** Image width in pixels. */
    public int col;
    /** Image height in pixels. */
    public int row;
    /** Reserved / unused field; must be zero. */
    public int unk;
    /** Row stride in bytes (typically {@code width * 4}). */
    public long step;
    /** Raw pointer to the pixel data buffer. */
    public long dataPtr;

    @Override
    protected List<String> getFieldOrder() {
      return List.of("t", "col", "row", "unk", "step", "dataPtr");
    }

    public static class ByReference extends Img implements Structure.ByReference {
    }
  }

  /**
   * Mirrors the native {@code OcrBoundingBox} struct returned for each recognised word or line. The
   * box is represented as four corners in clockwise order starting from the top-left: (x1,y1) →
   * (x2,y2) → (x3,y3) → (x4,y4).
   *
   * <pre>
   * struct OcrBoundingBox { float X1, Y1, X2, Y2, X3, Y3, X4, Y4; }
   * </pre>
   */
  public static class OcrBoundingBox extends Structure {
    public float x1, y1, x2, y2, x3, y3, x4, y4;

    @Override
    protected List<String> getFieldOrder() {
      return List.of("x1", "y1", "x2", "y2", "x3", "y3", "x4", "y4");
    }
  }

  // -------------------------------------------------------------------------
  // Instance state
  // -------------------------------------------------------------------------

  private final OneOcrLib lib;
  private final Pointer pipeline;
  private final DiskImageWriter diskImageWriter;

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /**
   * Loads the native OneOCR library and initialises the recognition pipeline.
   *
   * @param preprocessingManipulations image manipulations applied before OCR
   * @param diskImageWriter used to persist the screenshot before passing it to the native library
   */
  public OneOcr(List<ImageManipulation> preprocessingManipulations,
      DiskImageWriter diskImageWriter) {
    super(preprocessingManipulations);
    this.diskImageWriter = diskImageWriter;

    // Extend the DLL search path so that oneocr.dll can find its sibling DLLs.
    Kernel32 kernel32 = Native.load("kernel32", Kernel32.class);
    kernel32.SetDllDirectoryA(WRAPPER_DIR);

    try {
      lib = Native.load(DLL_PATH, OneOcrLib.class);
      pipeline = createPipeline();
    } finally {
      // Always restore the original DLL search path.
      kernel32.SetDllDirectoryA(null);
    }
  }

  // -------------------------------------------------------------------------
  // Ocr implementation
  // -------------------------------------------------------------------------

  @Override
  protected OcrResult process(BufferedImage image) {
    var imagePath = diskImageWriter.write(image, ImageType.SCREENSHOT)
        .orElseThrow(() -> new IllegalStateException(
            "Screenshot output is disabled; OneOcr requires the image to be written to disk"));

    BufferedImage bgra = loadImageFromDisk(imagePath);
    Img.ByReference img = toNativeImg(bgra);
    List<LocatedWord> locatedWords = runOcr(img);
    OcrResult ocrResult = new OcrResult(locatedWords);

    return ocrResult;
  }

  // -------------------------------------------------------------------------
  // Pipeline initialisation
  // -------------------------------------------------------------------------

  private Pointer createPipeline() {
    Pointer ctx = createInitOptions();

    check("OcrInitOptionsSetUseModelDelayLoad",
        lib.OcrInitOptionsSetUseModelDelayLoad(ctx, DISABLE_MODEL_DELAY_LOAD));

    // Memory instances keep the native buffers alive for the duration of the call.
    Memory modelMem = toNativeString(MODEL_PATH);
    Memory keyMem = toNativeString(MODEL_KEY);

    var pipelineRef = new PointerByReference();
    check("CreateOcrPipeline", lib.CreateOcrPipeline(modelMem, keyMem, ctx, pipelineRef));
    return pipelineRef.getValue();
  }

  private Pointer createInitOptions() {
    var ctxRef = new PointerByReference();
    check("CreateOcrInitOptions", lib.CreateOcrInitOptions(ctxRef));
    return ctxRef.getValue();
  }

  // -------------------------------------------------------------------------
  // OCR execution
  // -------------------------------------------------------------------------

  private BufferedImage loadImageFromDisk(Path imagePath) {
    BufferedImage loaded;
    try {
      loaded = ImageIO.read(imagePath.toFile());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read image: " + imagePath, e);
    }

    // Mirror the C# .Clone(..., Format32bppArgb) step: produce a 32bpp ARGB image
    // whose backing int[] is laid out as BGRA bytes in native (little-endian) memory,
    // exactly matching what GDI+ exposes through BitmapData.Scan0.
    BufferedImage bgra =
        new BufferedImage(loaded.getWidth(), loaded.getHeight(), BufferedImage.TYPE_INT_ARGB);
    bgra.createGraphics().drawImage(loaded, 0, 0, null);

    return bgra;
  }

  private List<LocatedWord> runOcr(Img.ByReference img) {
    Pointer opt = createProcessOptions();

    var instanceRef = new PointerByReference();
    check("RunOcrPipeline", lib.RunOcrPipeline(pipeline, img, opt, instanceRef));
    Pointer instance = instanceRef.getValue();

    var lineCountRef = new LongByReference();
    lib.GetOcrLineCount(instance, lineCountRef);

    return toLocatedWords(instance, lineCountRef.getValue());
  }

  private Pointer createProcessOptions() {
    var optRef = new PointerByReference();
    check("CreateOcrProcessOptions", lib.CreateOcrProcessOptions(optRef));
    Pointer opt = optRef.getValue();

    check("OcrProcessOptionsSetMaxRecognitionLineCount",
        lib.OcrProcessOptionsSetMaxRecognitionLineCount(opt, MAX_RECOGNITION_LINES));

    return opt;
  }

  private List<LocatedWord> toLocatedWords(Pointer instance, long lineCount) {
    var words = new ArrayList<LocatedWord>();

    for (long i = 0; i < lineCount; i++) {
      var lineRef = new PointerByReference();
      lib.GetOcrLine(instance, i, lineRef);
      Pointer line = lineRef.getValue();

      if (isNullPointer(line)) {
        continue;
      }

      toLocatedWords(line, words);
    }

    return Collections.unmodifiableList(words);
  }

  private void toLocatedWords(Pointer line, List<LocatedWord> words) {
    var wordCountRef = new LongByReference();
    lib.GetOcrLineWordCount(line, wordCountRef);
    long wordCount = wordCountRef.getValue();

    for (long j = 0; j < wordCount; j++) {
      var wordRef = new PointerByReference();
      lib.GetOcrWord(line, j, wordRef);
      Pointer word = wordRef.getValue();

      if (isNullPointer(word)) {
        continue;
      }

      words.add(toLocatedWord(word));
    }
  }

  private LocatedWord toLocatedWord(Pointer word) {
    var textRef = new PointerByReference();
    lib.GetOcrWordContent(word, textRef);
    String text = textRef.getValue().getString(0, StandardCharsets.UTF_8.name());

    var boxRef = new PointerByReference();
    lib.GetOcrWordBoundingBox(word, boxRef);
    OcrBoundingBox box = Structure.newInstance(OcrBoundingBox.class, boxRef.getValue());
    box.read();

    return new LocatedWord(text.toLowerCase(), toRectangle(box));
  }

  // -------------------------------------------------------------------------
  // Static helpers
  // -------------------------------------------------------------------------

  /**
   * Converts a {@link BufferedImage} to a native {@link Img} struct with BGRA pixel layout,
   * matching the format expected by the native OCR pipeline. The pixel data is copied into a
   * contiguous native buffer, and the returned {@code Img} struct points to this buffer. The caller
   * must ensure that the native buffer remains valid for the duration of the OCR call (e.g. by
   * keeping a reference to the returned struct and allowing the GC to clean up the native memory
   * when no longer needed).
   *
   * @param image the input image, expected to be in ARGB format (e.g. loaded with
   *        BufferedImage.TYPE_INT_ARGB)
   * @return a native-compatible {@code Img} struct referencing the pixel data
   */
  private static Img.ByReference toNativeImg(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();

    int[] pixels = new int[width * height];
    image.getRGB(0, 0, width, height, pixels, 0, width);

    Memory pixelMem = new Memory((long) pixels.length * Integer.BYTES);
    pixelMem.write(0, pixels, 0, pixels.length);

    Img.ByReference img = new Img.ByReference();
    img.t = 3; // 4-channel image type
    img.col = width;
    img.row = height;
    img.unk = 0;
    img.step = (long) width * Integer.BYTES;
    img.dataPtr = Pointer.nativeValue(pixelMem);
    return img;
  }

  /**
   * Converts a quadrilateral {@link OcrBoundingBox} to an axis-aligned {@link Rectangle} using the
   * top-left (x1,y1) and bottom-right (x3,y3) corners.
   */
  private static Rectangle toRectangle(OcrBoundingBox box) {
    int x = Math.round(box.x1);
    int y = Math.round(box.y1);
    int width = Math.round(box.x3 - box.x1);
    int height = Math.round(box.y3 - box.y1);
    return new Rectangle(x, y, width, height);
  }

  /**
   * Encodes {@code s} as ASCII and appends a null terminator into a native {@link Memory} buffer.
   */
  private static Memory toNativeString(String s) {
    byte[] encoded = s.getBytes(StandardCharsets.US_ASCII);
    Memory mem = new Memory(encoded.length + 1L); // +1 for null terminator
    mem.write(0, encoded, 0, encoded.length);
    mem.setByte(encoded.length, (byte) 0);
    return mem;
  }

  /** Returns {@code true} if {@code ptr} is null or points to address zero. */
  private static boolean isNullPointer(Pointer ptr) {
    return ptr == null || Pointer.nativeValue(ptr) == 0;
  }

  /**
   * Asserts that a native API call succeeded (return code {@code 0}).
   *
   * @throws IllegalStateException if {@code result} is non-zero
   */
  private static void check(String functionName, long result) {
    if (result != 0) {
      throw new IllegalStateException(
          "Native call '" + functionName + "' failed with error code: " + result);
    }
  }
}
