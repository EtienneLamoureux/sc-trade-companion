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
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageType;
import tools.sctrade.companion.output.DiskImageWriter;

/**
 * A straight forward re-implementation of the following c# code in java using JNA ```c# using
 * System; using System.Collections.Generic; using System.Drawing; using System.Drawing.Imaging;
 * using System.Runtime.InteropServices; using System.Text.Json; using
 * System.Text.Json.Serialization;
 * 
 * #region Native structs
 * 
 * [StructLayout(LayoutKind.Sequential, Pack = 1)] struct Img { public int T; public int Col; public
 * int Row; public int Unk; public long Step; public long DataPtr; }
 * 
 * struct OcrBoundingBox { public float X1; public float Y1; public float X2; public float Y2;
 * public float X3; public float Y3; public float X4; public float Y4; }
 * 
 * #endregion
 * 
 * #region PInvoke
 * 
 * static class OneOcr { const string DLL = "oneocr.dll";
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * CreateOcrInitOptions(out IntPtr ctx);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * OcrInitOptionsSetUseModelDelayLoad(IntPtr ctx, byte flag);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * CreateOcrPipeline( IntPtr modelPath, IntPtr key, IntPtr ctx, out IntPtr pipeline);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * CreateOcrProcessOptions(out IntPtr opt);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * OcrProcessOptionsSetMaxRecognitionLineCount(IntPtr opt, long count);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * RunOcrPipeline( IntPtr pipeline, ref Img img, IntPtr opt, out IntPtr instance);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * GetOcrLineCount(IntPtr instance, out long count);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * GetOcrLine(IntPtr instance, long index, out IntPtr line);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * GetOcrLineContent(IntPtr line, out IntPtr textPtr);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * GetOcrLineBoundingBox(IntPtr line, out IntPtr boxPtr);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * GetOcrLineWordCount(IntPtr line, out long count);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * GetOcrWord(IntPtr line, long index, out IntPtr word);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * GetOcrWordContent(IntPtr word, out IntPtr textPtr);
 * 
 * [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)] public static extern long
 * GetOcrWordBoundingBox(IntPtr word, out IntPtr boxPtr); }
 * 
 * #endregion
 * 
 * #region DTOs
 * 
 * class BoundingBox { public BoundingBox(OcrBoundingBox box) { X = box.X1; Y = box.Y1; Width =
 * box.X3 - X; Height = box.Y3 - Y; }
 * 
 * [JsonPropertyName("x")] public float X { get; } [JsonPropertyName("y")] public float Y { get; }
 * [JsonPropertyName("width")] public float Width { get; } [JsonPropertyName("height")] public float
 * Height { get; } }
 * 
 * class OcrWordDto { [JsonPropertyName("text")] public string Text { get; set; }
 * [JsonPropertyName("boundingBox")] public BoundingBox BoundingBox { get; set; } }
 * 
 * class OcrLineDto { [JsonPropertyName("text")] public string Text { get; set; }
 * [JsonPropertyName("boundingBox")] public BoundingBox BoundingBox { get; set; }
 * [JsonPropertyName("words")] public List<OcrWordDto> Words { get; set; } = new List<OcrWordDto>();
 * }
 * 
 * class OcrResultDto { [JsonPropertyName("lines")] public List<OcrLineDto> Lines { get; set; } =
 * new List<OcrLineDto>(); }
 * 
 * #endregion
 * 
 * class Program { static void Main(string[] args) { if (args.Length < 1) {
 * Console.WriteLine("Usage: OneOcrWrapper.exe <image.png|bmp|jpg> [--pretty-print]"); return; }
 * 
 * bool prettyPrint = args.Length > 1 && args[1] == "--pretty-print";
 * 
 * using Bitmap bmp = new Bitmap(args[0]); using Bitmap bgra = bmp.Clone(new Rectangle(0, 0,
 * bmp.Width, bmp.Height), PixelFormat.Format32bppArgb);
 * 
 * int width = bgra.Width; int height = bgra.Height;
 * 
 * BitmapData bmpData = bgra.LockBits( new Rectangle(0, 0, width, height), ImageLockMode.ReadOnly,
 * PixelFormat.Format32bppArgb);
 * 
 * try { Img img = new Img { T = 3, Col = width, Row = height, Unk = 0, Step = bmpData.Stride,
 * DataPtr = bmpData.Scan0.ToInt64() };
 * 
 * var result = RunOcr(img); OutputJson(result, prettyPrint); } catch (Exception ex) {
 * Console.Error.WriteLine($"Error: {ex.Message}"); } finally { bgra.UnlockBits(bmpData); } }
 * 
 * static byte[] StringToAnsiBytes(string s) { // null-terminated ANSI bytes, matching const char*
 * in C++ var bytes = System.Text.Encoding.Default.GetBytes(s); var result = new byte[bytes.Length +
 * 1]; Buffer.BlockCopy(bytes, 0, result, 0, bytes.Length); return result; }
 * 
 * static void OutputJson(OcrResultDto resultDto, bool prettyPrint) { string jsonOutput =
 * JsonSerializer.Serialize(resultDto, new JsonSerializerOptions { WriteIndented = prettyPrint });
 * Console.WriteLine(jsonOutput); }
 * 
 * static OcrResultDto RunOcr(Img img) { IntPtr ctx, pipeline, opt, instance;
 * 
 * long res;
 * 
 * res = OneOcr.CreateOcrInitOptions(out ctx); if (res != 0) throw new
 * Exception($"CreateOcrInitOptions failed: {res}");
 * 
 * res = OneOcr.OcrInitOptionsSetUseModelDelayLoad(ctx, 0); if (res != 0) throw new
 * Exception($"OcrInitOptionsSetUseModelDelayLoad failed: {res}");
 * 
 * string model = "oneocr.onemodel"; string key = "kj)TGtrK>f]b[Piow.gU+nC@s\"\"\"\"\"\"4";
 * 
 * // Pin byte arrays so the native side gets stable const char* pointers byte[] modelBytes =
 * StringToAnsiBytes(model); byte[] keyBytes = StringToAnsiBytes(key);
 * 
 * GCHandle modelHandle = GCHandle.Alloc(modelBytes, GCHandleType.Pinned); GCHandle keyHandle =
 * GCHandle.Alloc(keyBytes, GCHandleType.Pinned);
 * 
 * try { IntPtr modelPtr = modelHandle.AddrOfPinnedObject(); IntPtr keyPtr =
 * keyHandle.AddrOfPinnedObject();
 * 
 * res = OneOcr.CreateOcrPipeline(modelPtr, keyPtr, ctx, out pipeline); if (res != 0) throw new
 * Exception($"CreateOcrPipeline failed: {res}");
 * 
 * res = OneOcr.CreateOcrProcessOptions(out opt); if (res != 0) throw new
 * Exception($"CreateOcrProcessOptions failed: {res}");
 * 
 * res = OneOcr.OcrProcessOptionsSetMaxRecognitionLineCount(opt, 1000); if (res != 0) throw new
 * Exception($"OcrProcessOptionsSetMaxRecognitionLineCount failed: {res}");
 * 
 * res = OneOcr.RunOcrPipeline(pipeline, ref img, opt, out instance); if (res != 0) throw new
 * Exception($"RunOcrPipeline failed: {res}"); } finally { modelHandle.Free(); keyHandle.Free(); }
 * 
 * OneOcr.GetOcrLineCount(instance, out long lines);
 * 
 * return CreateOcrResultDto(instance, lines); }
 * 
 * static OcrResultDto CreateOcrResultDto(IntPtr instance, long lines) { var resultDto = new
 * OcrResultDto();
 * 
 * for (long i = 0; i < lines; i++) { OneOcr.GetOcrLine(instance, i, out IntPtr line); if (line ==
 * IntPtr.Zero) continue;
 * 
 * OneOcr.GetOcrLineContent(line, out IntPtr textPtr); string text =
 * Marshal.PtrToStringAnsi(textPtr);
 * 
 * OneOcr.GetOcrLineBoundingBox(line, out IntPtr boxPtr); OcrBoundingBox box =
 * Marshal.PtrToStructure<OcrBoundingBox>(boxPtr);
 * 
 * var lineDto = new OcrLineDto { Text = text, BoundingBox = new BoundingBox(box) };
 * 
 * OneOcr.GetOcrLineWordCount(line, out long wc); for (long j = 0; j < wc; j++) {
 * OneOcr.GetOcrWord(line, j, out IntPtr word); OneOcr.GetOcrWordContent(word, out IntPtr wptr);
 * OneOcr.GetOcrWordBoundingBox(word, out IntPtr wboxPtr); OcrBoundingBox wbox =
 * Marshal.PtrToStructure<OcrBoundingBox>(wboxPtr);
 * 
 * string wtext = Marshal.PtrToStringAnsi(wptr);
 * 
 * lineDto.Words.Add(new OcrWordDto { Text = wtext, BoundingBox = new BoundingBox(wbox) }); }
 * 
 * resultDto.Lines.Add(lineDto); }
 * 
 * return resultDto; } }
 * 
 * ```
 */
public class OneOcr extends Ocr {
  private static final String WRAPPER_DIR =
      Paths.get("bin/oneocr-wrapper").toAbsolutePath().toString();
  private static final String DLL_PATH = WRAPPER_DIR + "/oneocr";
  private static final String MODEL_PATH = WRAPPER_DIR + "/oneocr.onemodel";
  private static final String MODEL_KEY = "kj)TGtrK>f]b[Piow.gU+nC@s\"\"\"\"\"\"4";
  private static final long MAX_LINES = 1000;

  private final Logger logger = LoggerFactory.getLogger(OneOcr.class);

  // -------------------------------------------------------------------------
  // JNA interface
  // -------------------------------------------------------------------------

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

  // -------------------------------------------------------------------------
  // Native structs
  // -------------------------------------------------------------------------

  /**
   * [StructLayout(LayoutKind.Sequential, Pack = 1)] struct Img { int T; int Col; int Row; int Unk;
   * long Step; long DataPtr; }
   */
  public static class Img extends Structure {
    public int t;
    public int col;
    public int row;
    public int unk;
    public long step;
    public long dataPtr;

    @Override
    protected List<String> getFieldOrder() {
      return List.of("t", "col", "row", "unk", "step", "dataPtr");
    }

    public static class ByReference extends Img implements Structure.ByReference {
    }
  }

  /**
   * struct OcrBoundingBox { float X1, Y1, X2, Y2, X3, Y3, X4, Y4; }
   */
  public static class OcrBoundingBox extends Structure {
    public float x1, y1, x2, y2, x3, y3, x4, y4;

    @Override
    protected List<String> getFieldOrder() {
      return List.of("x1", "y1", "x2", "y2", "x3", "y3", "x4", "y4");
    }
  }

  // Used to add the wrapper directory to the Windows DLL search path so that
  // oneocr.dll can resolve its siblings (onnxruntime.dll, opencv_world460.dll, …).
  private interface Kernel32 extends Library {
    boolean SetDllDirectoryA(String path);
  }

  // -------------------------------------------------------------------------
  // Lifecycle
  // -------------------------------------------------------------------------

  private final OneOcrLib lib;
  private final Pointer pipeline;
  private final DiskImageWriter diskImageWriter;

  public OneOcr(List<ImageManipulation> preprocessingManipulations,
      DiskImageWriter diskImageWriter) {
    super(preprocessingManipulations);
    this.diskImageWriter = diskImageWriter;

    // Tell Windows to look in the wrapper dir when resolving DLL dependencies.
    Kernel32 kernel32 = Native.load("kernel32", Kernel32.class);
    kernel32.SetDllDirectoryA(WRAPPER_DIR);

    try {
      lib = Native.load(DLL_PATH, OneOcrLib.class);
      pipeline = createPipeline();
    } finally {
      kernel32.SetDllDirectoryA(null);
    }
  }

  // -------------------------------------------------------------------------
  // Ocr implementation
  // -------------------------------------------------------------------------

  @Override
  protected OcrResult process(BufferedImage image) {
    // Write to disk so that the image goes through the same encoding/decoding path
    // as the C# wrapper, then reload it without ICC profile application to get the
    // same raw pixel values that System.Drawing.Bitmap produces.
    var imagePath = diskImageWriter.write(image, ImageType.SCREENSHOT).orElseThrow();
    BufferedImage reloaded = readIgnoringIccProfile(imagePath.toFile());

    int width = reloaded.getWidth();
    int height = reloaded.getHeight();

    int[] pixels = new int[width * height];
    reloaded.getRGB(0, 0, width, height, pixels, 0, width);

    Memory pixelMem = new Memory((long) pixels.length * Integer.BYTES);
    pixelMem.write(0, pixels, 0, pixels.length);

    Img.ByReference img = new Img.ByReference();
    img.t = 3;
    img.col = width;
    img.row = height;
    img.unk = 0;
    img.step = (long) width * 4;
    img.dataPtr = Pointer.nativeValue(pixelMem);

    var words = runOcr(img);
    return new OcrResult(words);
  }

  private static BufferedImage readIgnoringIccProfile(File file) {
    try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
      var readers = ImageIO.getImageReaders(iis);

      if (!readers.hasNext()) {
        throw new RuntimeException("No ImageReader found for file: " + file);
      }

      ImageReader reader = readers.next();

      try {
        reader.setInput(iis, true, true); // ignoreMetadata=true skips ICC profile
        return reader.read(0, reader.getDefaultReadParam());
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read image: " + file, e);
    }
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private Pointer createPipeline() {
    var ctxRef = new PointerByReference();
    check("CreateOcrInitOptions", lib.CreateOcrInitOptions(ctxRef));
    Pointer ctx = ctxRef.getValue();

    check("OcrInitOptionsSetUseModelDelayLoad",
        lib.OcrInitOptionsSetUseModelDelayLoad(ctx, (byte) 0));

    byte[] modelBytes = toNullTerminatedAnsi(MODEL_PATH);
    byte[] keyBytes = toNullTerminatedAnsi(MODEL_KEY);

    // Memory keeps the native buffer alive and valid for the duration of the call
    Memory modelMem = new Memory(modelBytes.length);
    Memory keyMem = new Memory(keyBytes.length);
    modelMem.write(0, modelBytes, 0, modelBytes.length);
    keyMem.write(0, keyBytes, 0, keyBytes.length);

    var pipelineRef = new PointerByReference();
    check("CreateOcrPipeline", lib.CreateOcrPipeline(modelMem, keyMem, ctx, pipelineRef));
    return pipelineRef.getValue();
  }

  private List<LocatedWord> runOcr(Img.ByReference img) {
    var optRef = new PointerByReference();
    check("CreateOcrProcessOptions", lib.CreateOcrProcessOptions(optRef));
    Pointer opt = optRef.getValue();

    check("OcrProcessOptionsSetMaxRecognitionLineCount",
        lib.OcrProcessOptionsSetMaxRecognitionLineCount(opt, MAX_LINES));

    var instanceRef = new PointerByReference();
    check("RunOcrPipeline", lib.RunOcrPipeline(pipeline, img, opt, instanceRef));
    Pointer instance = instanceRef.getValue();

    var lineCountRef = new LongByReference();
    lib.GetOcrLineCount(instance, lineCountRef);
    long lineCount = lineCountRef.getValue();

    return collectWords(instance, lineCount);
  }

  private List<LocatedWord> collectWords(Pointer instance, long lineCount) {
    var words = new ArrayList<LocatedWord>();

    for (long i = 0; i < lineCount; i++) {
      var lineRef = new PointerByReference();
      lib.GetOcrLine(instance, i, lineRef);
      Pointer line = lineRef.getValue();

      if (line == null || Pointer.nativeValue(line) == 0) {
        continue;
      }

      var wordCountRef = new LongByReference();
      lib.GetOcrLineWordCount(line, wordCountRef);
      long wordCount = wordCountRef.getValue();

      for (long j = 0; j < wordCount; j++) {
        var wordRef = new PointerByReference();
        lib.GetOcrWord(line, j, wordRef);
        Pointer word = wordRef.getValue();

        if (word == null || Pointer.nativeValue(word) == 0) {
          continue;
        }

        var textRef = new PointerByReference();
        lib.GetOcrWordContent(word, textRef);
        String text = textRef.getValue().getString(0, "UTF-8");

        var boxRef = new PointerByReference();
        lib.GetOcrWordBoundingBox(word, boxRef);
        OcrBoundingBox box = Structure.newInstance(OcrBoundingBox.class, boxRef.getValue());
        box.read();

        Rectangle rect = toRectangle(box);
        words.add(new LocatedWord(text.toLowerCase(), rect));
      }
    }

    return Collections.unmodifiableList(words);
  }

  private static Rectangle toRectangle(OcrBoundingBox box) {
    int x = Math.round(box.x1);
    int y = Math.round(box.y1);
    int width = Math.round(box.x3 - box.x1);
    int height = Math.round(box.y3 - box.y1);
    return new Rectangle(x, y, width, height);
  }

  private static byte[] toNullTerminatedAnsi(String s) {
    byte[] encoded = s.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    byte[] result = new byte[encoded.length + 1];
    System.arraycopy(encoded, 0, result, 0, encoded.length);
    return result;
  }

  private static void check(String name, long result) {
    if (result != 0) {
      throw new RuntimeException(name + " failed with code: " + result);
    }
  }
}
