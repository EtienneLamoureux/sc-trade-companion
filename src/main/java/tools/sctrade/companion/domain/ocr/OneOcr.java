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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;

/**
 * JNA-based OCR engine that calls {@code oneocr.dll} directly, equivalent to the OneOcrWrapper.exe
 * C# process.
 */
public class OneOcr extends Ocr {

  // region Native structs

  /**
   * Mirrors the native {@code Img} struct (Pack=1).
   */
  @Structure.FieldOrder({"t", "col", "row", "unk", "step", "dataPtr"})
  public static class Img extends Structure {
    public Img() {
      super(ALIGN_NONE);
    }

    public int t;
    public int col;
    public int row;
    public int unk;
    public long step;
    public long dataPtr;
  }

  /**
   * Mirrors the native {@code OcrBoundingBox} struct (8 floats).
   */
  @Structure.FieldOrder({"x1", "y1", "x2", "y2", "x3", "y3", "x4", "y4"})
  public static class OcrBoundingBox extends Structure {
    public float x1;
    public float y1;
    public float x2;
    public float y2;
    public float x3;
    public float y3;
    public float x4;
    public float y4;
  }

  // endregion

  // region JNA interface

  interface OneOcrLib extends Library {
    long CreateOcrInitOptions(PointerByReference ctx);

    long OcrInitOptionsSetUseModelDelayLoad(Pointer ctx, byte flag);

    long CreateOcrPipeline(Pointer modelPath, Pointer key, Pointer ctx,
        PointerByReference pipeline);

    long CreateOcrProcessOptions(PointerByReference opt);

    long OcrProcessOptionsSetMaxRecognitionLineCount(Pointer opt, long count);

    long RunOcrPipeline(Pointer pipeline, Img img, Pointer opt, PointerByReference instance);

    long GetOcrLineCount(Pointer instance, LongByReference count);

    long GetOcrLine(Pointer instance, long index, PointerByReference line);

    long GetOcrLineContent(Pointer line, PointerByReference textPtr);

    long GetOcrLineBoundingBox(Pointer line, PointerByReference boxPtr);

    long GetOcrLineWordCount(Pointer line, LongByReference count);

    long GetOcrWord(Pointer line, long index, PointerByReference word);

    long GetOcrWordContent(Pointer word, PointerByReference textPtr);

    long GetOcrWordBoundingBox(Pointer word, PointerByReference boxPtr);
  }

  // endregion

  private static final String DLL_NAME = "bin/oneocr-wrapper/oneocr";
  private static final String MODEL_PATH = "bin/oneocr-wrapper/oneocr.onemodel";
  private static final String KEY = "kj)TGtrK>f]b[Piow.gU+nC@s\"\"\"\"\"\"4";
  private static final long MAX_LINE_COUNT = 1000L;

  private final Logger logger = LoggerFactory.getLogger(OneOcr.class);

  private final OneOcrLib lib;
  // Kept as fields to prevent GC while native code holds raw pointers
  private final Memory modelMem = toAnsiMemory(MODEL_PATH);
  private final Memory keyMem = toAnsiMemory(KEY);
  private Memory pixelMemory;

  public OneOcr(List<ImageManipulation> preprocessingManipulations) {
    super(preprocessingManipulations);
    this.lib = Native.load(DLL_NAME, OneOcrLib.class);
  }

  @Override
  protected OcrResult process(BufferedImage image) {
    // Convert to BGRA (TYPE_4BYTE_ABGR stored as BGRA by AWT on little-endian)
    BufferedImage bgra =
        new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
    bgra.getGraphics().drawImage(image, 0, 0, null);

    int width = bgra.getWidth();
    int height = bgra.getHeight();
    int[] argbPixels = bgra.getRGB(0, 0, width, height, null, 0, width);

    // Pack pixels into a native memory buffer as 4-byte BGRA
    int stride = width * 4;
    Memory pixelMemory = new Memory((long) height * stride);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = argbPixels[y * width + x];
        byte b = (byte) (argb & 0xFF);
        byte g = (byte) ((argb >> 8) & 0xFF);
        byte r = (byte) ((argb >> 16) & 0xFF);
        byte a = (byte) ((argb >> 24) & 0xFF);
        long offset = (long) y * stride + (long) x * 4;
        pixelMemory.setByte(offset, b);
        pixelMemory.setByte(offset + 1, g);
        pixelMemory.setByte(offset + 2, r);
        pixelMemory.setByte(offset + 3, a);
      }
    }

    Img img = new Img();
    img.t = 3;
    img.col = width;
    img.row = height;
    img.unk = 0;
    img.step = stride;
    img.dataPtr = Pointer.nativeValue(pixelMemory);

    try {
      return runOcr(img);
    } catch (Exception e) {
      logger.error("OneOCR failed", e);
      return new OcrResult(List.of());
    }
  }

  private OcrResult runOcr(Img img) {
    long res;

    PointerByReference ctxRef = new PointerByReference();
    res = lib.CreateOcrInitOptions(ctxRef);
    check(res, "CreateOcrInitOptions");

    Pointer ctx = ctxRef.getValue();
    res = lib.OcrInitOptionsSetUseModelDelayLoad(ctx, (byte) 0);
    check(res, "OcrInitOptionsSetUseModelDelayLoad");

    Memory modelMem = toAnsiMemory(MODEL_PATH);
    Memory keyMem = toAnsiMemory(KEY);

    PointerByReference pipelineRef = new PointerByReference();
    res = lib.CreateOcrPipeline(modelMem, keyMem, ctx, pipelineRef);
    check(res, "CreateOcrPipeline");

    Pointer pipeline = pipelineRef.getValue();

    PointerByReference optRef = new PointerByReference();
    res = lib.CreateOcrProcessOptions(optRef);
    check(res, "CreateOcrProcessOptions");

    Pointer opt = optRef.getValue();
    res = lib.OcrProcessOptionsSetMaxRecognitionLineCount(opt, MAX_LINE_COUNT);
    check(res, "OcrProcessOptionsSetMaxRecognitionLineCount");

    PointerByReference instanceRef = new PointerByReference();
    res = lib.RunOcrPipeline(pipeline, img, opt, instanceRef);
    check(res, "RunOcrPipeline");

    Pointer instance = instanceRef.getValue();

    LongByReference lineCountRef = new LongByReference();
    lib.GetOcrLineCount(instance, lineCountRef);
    long lineCount = lineCountRef.getValue();

    return buildOcrResult(instance, lineCount);
  }

  private OcrResult buildOcrResult(Pointer instance, long lineCount) {
    List<LocatedWord> words = new ArrayList<>();

    for (long i = 0; i < lineCount; i++) {
      PointerByReference lineRef = new PointerByReference();
      lib.GetOcrLine(instance, i, lineRef);
      Pointer line = lineRef.getValue();

      if (line == null || Pointer.nativeValue(line) == 0) {
        continue;
      }

      LongByReference wordCountRef = new LongByReference();
      lib.GetOcrLineWordCount(line, wordCountRef);
      long wordCount = wordCountRef.getValue();

      for (long j = 0; j < wordCount; j++) {
        PointerByReference wordRef = new PointerByReference();
        lib.GetOcrWord(line, j, wordRef);
        Pointer word = wordRef.getValue();

        if (word == null || Pointer.nativeValue(word) == 0) {
          continue;
        }

        PointerByReference wTextRef = new PointerByReference();
        lib.GetOcrWordContent(word, wTextRef);
        String text = wTextRef.getValue().getString(0, StandardCharsets.US_ASCII.name());

        PointerByReference wBoxRef = new PointerByReference();
        lib.GetOcrWordBoundingBox(word, wBoxRef);
        OcrBoundingBox nativeBox = Structure.newInstance(OcrBoundingBox.class, wBoxRef.getValue());
        nativeBox.read();

        Rectangle rect = toRectangle(nativeBox);
        words.add(new LocatedWord(text.toLowerCase(), rect));
      }
    }

    return new OcrResult(words);
  }

  private static void check(long result, String method) {
    if (result != 0) {
      throw new IllegalStateException(method + " failed with code: " + result);
    }
  }

  private static Memory toAnsiMemory(String s) {
    byte[] bytes = s.getBytes(StandardCharsets.US_ASCII);
    Memory mem = new Memory(bytes.length + 1L);
    mem.write(0, bytes, 0, bytes.length);
    mem.setByte(bytes.length, (byte) 0);
    return mem;
  }

  private static Rectangle toRectangle(OcrBoundingBox box) {
    int x = Math.round(box.x1);
    int y = Math.round(box.y1);
    int width = Math.round(box.x3 - box.x1);
    int height = Math.round(box.y3 - box.y1);
    return new Rectangle(x, y, width, height);
  }
}