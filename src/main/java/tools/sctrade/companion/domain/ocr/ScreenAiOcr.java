package tools.sctrade.companion.domain.ocr;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;

/**
 * OCR implementation backed by Chrome's Screen AI library via JNA. File-reading callbacks are
 * provided by a small native helper library ({@code screenai_helper}) to avoid JVM thread
 * attachment issues with the library's internal background threads.
 *
 * <p>
 * The native library is loaded from {@code ~/.config/screen_ai/resources/} and will be
 * automatically downloaded from Google's CIPD infrastructure if not present. This engine works on
 * Windows, Linux, and macOS.
 */
public class ScreenAiOcr extends Ocr {
  private static final Logger logger = LoggerFactory.getLogger(ScreenAiOcr.class);

  // -------------------------------------------------------------------------
  // Constants
  // -------------------------------------------------------------------------
  private static final Path MODEL_DIR =
      Path.of(System.getProperty("user.home"), ".config", "screen_ai", "resources");
  private static final Path HELPER_DIR = Path.of("bin", "screenai").toAbsolutePath();
  private static final String CIPD_BASE_URL =
      "https://chrome-infra-packages.appspot.com/client?platform=%s&version=latest";
  private static final String CIPD_PACKAGE = "chromium/third_party/screen-ai/%s";

  // SkColorType and SkAlphaType constants matching Skia's enum values.
  private static final int SK_COLOR_TYPE_BGRA_8888 = 4;
  private static final int SK_ALPHA_TYPE_OPAQUE = 1;

  // -------------------------------------------------------------------------
  // JNA interfaces
  // -------------------------------------------------------------------------

  /** JNA binding for the Chrome Screen AI native library. */
  interface ScreenAiLib extends Library {
    void SetFileContentFunctions(Pointer getFileContentSize, Pointer getFileContent);

    boolean InitOCRUsingCallback();

    void SetOCRLightMode(boolean lightMode);

    int GetMaxImageDimension();

    Pointer PerformOCR(SkBitmap.ByReference bitmap, IntByReference outputLength);

    void FreeLibraryAllocatedCharArray(Pointer ptr);
  }

  // -------------------------------------------------------------------------
  // Native structs (Skia bitmap layout)
  // -------------------------------------------------------------------------

  /** Mirrors Skia's {@code SkColorInfo} struct. */
  public static class SkColorInfo extends Structure {
    public Pointer fColorSpace;
    public int fColorType;
    public int fAlphaType;

    @Override
    protected List<String> getFieldOrder() {
      return List.of("fColorSpace", "fColorType", "fAlphaType");
    }
  }

  /** Mirrors Skia's {@code SkISize} struct. */
  public static class SkISize extends Structure {
    public int fWidth;
    public int fHeight;

    @Override
    protected List<String> getFieldOrder() {
      return List.of("fWidth", "fHeight");
    }
  }

  /** Mirrors Skia's {@code SkImageInfo} struct. */
  public static class SkImageInfo extends Structure {
    public SkColorInfo fColorInfo;
    public SkISize fDimensions;

    @Override
    protected List<String> getFieldOrder() {
      return List.of("fColorInfo", "fDimensions");
    }
  }

  /** Mirrors Skia's {@code SkPixmap} struct. */
  public static class SkPixmap extends Structure {
    public Pointer fPixels;
    public long fRowBytes;
    public SkImageInfo fInfo;

    @Override
    protected List<String> getFieldOrder() {
      return List.of("fPixels", "fRowBytes", "fInfo");
    }
  }

  /** Mirrors Skia's {@code SkBitmap} struct. */
  public static class SkBitmap extends Structure {
    public Pointer fPixelRef;
    public SkPixmap fPixmap;
    public int fFlags;

    @Override
    protected List<String> getFieldOrder() {
      return List.of("fPixelRef", "fPixmap", "fFlags");
    }

    public static class ByReference extends SkBitmap implements Structure.ByReference {
    }
  }

  // -------------------------------------------------------------------------
  // Instance state
  // -------------------------------------------------------------------------

  private final ScreenAiLib lib;
  private final int maxPixelSize;

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /**
   * Loads the Chrome Screen AI native library and initialises the OCR pipeline. If the model files
   * are not present, they will be downloaded automatically.
   *
   * @param preprocessingManipulations image manipulations applied before OCR
   */
  public ScreenAiOcr(List<ImageManipulation> preprocessingManipulations) {
    super(preprocessingManipulations);

    downloadModelIfNeeded();

    String dllName =
        System.getProperty("os.name").toLowerCase().contains("win") ? "chrome_screen_ai.dll"
            : "libchromescreenai.so";
    String libraryPath = MODEL_DIR.resolve(dllName).toAbsolutePath().toString();

    lib = Native.load(libraryPath, ScreenAiLib.class);

    // Load the native helper library that provides file-reading callbacks. These callbacks are
    // invoked from native threads with small stacks that cannot be attached to the JVM, so they
    // must be implemented in pure C rather than as JNA callbacks.
    boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    String helperName = isWindows ? "screenai_helper.dll" : "libscreenai_helper.so";
    String helperPath = HELPER_DIR.resolve(helperName).toString();

    // Use System.load to ensure the helper is loaded into the process, then look up function
    // pointers by symbol address. JNA's NativeLibrary wraps dlsym results in trampolines that
    // are not suitable for passing as raw C function pointers, so we resolve the addresses
    // directly using Native.
    System.load(helperPath);
    com.sun.jna.NativeLibrary helperNative = com.sun.jna.NativeLibrary.getInstance(helperPath);

    // Configure the helper with the model directory.
    com.sun.jna.Function setModelDir = helperNative.getFunction("set_model_dir");
    setModelDir.invoke(void.class, new Object[] {MODEL_DIR.toAbsolutePath().toString()});

    // Get raw function pointer addresses via reflection on JNA's package-private getSymbolAddress.
    // These are the actual dlsym results that SetFileContentFunctions expects.
    long getFileContentSizeAddr;
    long getFileContentAddr;
    try {
      var method =
          com.sun.jna.NativeLibrary.class.getDeclaredMethod("getSymbolAddress", String.class);
      method.setAccessible(true);
      getFileContentSizeAddr = (long) method.invoke(helperNative, "get_file_content_size");
      getFileContentAddr = (long) method.invoke(helperNative, "get_file_content");
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to resolve helper function addresses", e);
    }

    lib.SetFileContentFunctions(new Pointer(getFileContentSizeAddr),
        new Pointer(getFileContentAddr));
    lib.InitOCRUsingCallback();
    lib.SetOCRLightMode(false);
    maxPixelSize = lib.GetMaxImageDimension();

    logger.info("Chrome Screen AI ready (max dimension: {})", maxPixelSize);
  }

  // -------------------------------------------------------------------------
  // Ocr implementation
  // -------------------------------------------------------------------------

  @Override
  protected OcrResult process(BufferedImage image) {
    int originalWidth = image.getWidth();
    int originalHeight = image.getHeight();
    image = resizeIfNeeded(image);

    int width = image.getWidth();
    int height = image.getHeight();
    double scaleX = (double) originalWidth / width;
    double scaleY = (double) originalHeight / height;

    // Convert to RGBA pixel bytes.
    int[] argbPixels = new int[width * height];
    image.getRGB(0, 0, width, height, argbPixels, 0, width);
    byte[] rgbaBytes = argbToRgba(argbPixels);

    // Allocate native memory for pixel data.
    Memory pixelMem = new Memory(rgbaBytes.length);
    pixelMem.write(0, rgbaBytes, 0, rgbaBytes.length);

    // Build the SkBitmap struct.
    SkBitmap.ByReference bitmap = new SkBitmap.ByReference();
    bitmap.fPixelRef = Pointer.NULL;
    bitmap.fPixmap.fPixels = pixelMem;
    bitmap.fPixmap.fRowBytes = (long) width * 4;
    bitmap.fPixmap.fInfo.fColorInfo.fColorSpace = Pointer.NULL;
    bitmap.fPixmap.fInfo.fColorInfo.fColorType = SK_COLOR_TYPE_BGRA_8888;
    bitmap.fPixmap.fInfo.fColorInfo.fAlphaType = SK_ALPHA_TYPE_OPAQUE;
    bitmap.fPixmap.fInfo.fDimensions.fWidth = width;
    bitmap.fPixmap.fInfo.fDimensions.fHeight = height;
    bitmap.fFlags = 0;

    // Run OCR.
    var outputLength = new IntByReference(0);
    Pointer resultPtr = lib.PerformOCR(bitmap, outputLength);

    if (resultPtr == null || Pointer.nativeValue(resultPtr) == 0) {
      pixelMem.close();
      throw new IllegalStateException("PerformOCR returned null");
    }

    try {
      byte[] protoBytes = resultPtr.getByteArray(0, outputLength.getValue());
      List<LocatedWord> words = ScreenAiResponse.parseWords(protoBytes);

      // Scale bounding boxes back to original image coordinates if the image was resized.
      if (scaleX != 1.0 || scaleY != 1.0) {
        words = words.stream().map(w -> {
          var box = w.getBoundingBox();
          return new LocatedWord(w.getText(),
              new Rectangle((int) Math.round(box.getX() * scaleX),
                  (int) Math.round(box.getY() * scaleY), (int) Math.round(box.getWidth() * scaleX),
                  (int) Math.round(box.getHeight() * scaleY)));
        }).toList();
      }

      return new OcrResult(words);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to parse Screen AI response", e);
    } finally {
      lib.FreeLibraryAllocatedCharArray(resultPtr);
      pixelMem.close();
    }
  }

  // -------------------------------------------------------------------------
  // Model download
  // -------------------------------------------------------------------------

  private static void downloadModelIfNeeded() {
    if (Files.isDirectory(MODEL_DIR)) {
      logger.debug("Screen AI model directory exists: {}", MODEL_DIR);
      return;
    }

    logger.info("Downloading Screen AI model files to {}", MODEL_DIR.getParent());

    String packagePlatform = getCipdPackagePlatform();
    Path tempDir = null;

    try {
      tempDir = Files.createTempDirectory("screen_ai_cipd");
      Path cipdBin = downloadCipdClient(tempDir);

      String ensureContent = String.format(CIPD_PACKAGE, packagePlatform) + " latest\n";
      ProcessBuilder pb = new ProcessBuilder(cipdBin.toString(), "export", "-root",
          MODEL_DIR.getParent().toString(), "-ensure-file", "-");
      pb.redirectErrorStream(true);
      Process process = pb.start();

      // Write the ensure content to stdin.
      process.getOutputStream().write(ensureContent.getBytes());
      process.getOutputStream().close();

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new IOException("CIPD export failed with exit code: " + exitCode);
      }

      logger.info("Screen AI model files downloaded successfully");
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException("Failed to download Screen AI model files", e);
    } finally {
      deleteTempDir(tempDir);
    }
  }

  private static Path downloadCipdClient(Path tempDir) throws IOException {
    String url = String.format(CIPD_BASE_URL, getCipdClientPlatform());
    boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    String cipdName = isWindows ? "cipd.exe" : "cipd";
    Path cipdPath = tempDir.resolve(cipdName);

    logger.debug("Downloading CIPD client from {}", url);
    try (InputStream in = URI.create(url).toURL().openStream()) {
      Files.copy(in, cipdPath, StandardCopyOption.REPLACE_EXISTING);
    }

    if (!isWindows) {
      cipdPath.toFile().setExecutable(true);
    }

    return cipdPath;
  }

  /**
   * Returns the CIPD platform string in {@code os-arch} format (e.g. {@code linux-amd64},
   * {@code windows-amd64}, {@code mac-arm64}).
   */
  private static String getCipdClientPlatform() {
    String osName = System.getProperty("os.name").toLowerCase();
    String arch = System.getProperty("os.arch").toLowerCase();

    String os;
    if (osName.contains("win")) {
      os = "windows";
    } else if (osName.contains("mac")) {
      os = "mac";
    } else {
      os = "linux";
    }

    String cpuArch;
    if (arch.contains("amd64") || arch.contains("x86_64")) {
      cpuArch = "amd64";
    } else if (arch.contains("aarch64") || arch.contains("arm64")) {
      cpuArch = "arm64";
    } else {
      cpuArch = "386";
    }

    return os + "-" + cpuArch;
  }

  /**
   * Returns the CIPD platform string for the screen-ai package. The Linux package has no
   * architecture suffix, while Mac and Windows packages do.
   *
   * <p>
   * Available packages: {@code linux}, {@code mac-amd64}, {@code mac-arm64}, {@code windows-386},
   * {@code windows-amd64}.
   */
  private static String getCipdPackagePlatform() {
    String platform = getCipdClientPlatform();
    return platform.startsWith("linux") ? "linux" : platform;
  }

  private static void deleteTempDir(Path tempDir) {
    if (tempDir == null) {
      return;
    }
    try {
      Files.walk(tempDir).sorted(java.util.Comparator.reverseOrder()).map(Path::toFile)
          .forEach(java.io.File::delete);
    } catch (IOException e) {
      logger.warn("Failed to clean up temp directory: {}", tempDir, e);
    }
  }

  // -------------------------------------------------------------------------
  // Image helpers
  // -------------------------------------------------------------------------

  private BufferedImage resizeIfNeeded(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();

    if (width <= maxPixelSize && height <= maxPixelSize) {
      return image;
    }

    double scale = Math.min((double) maxPixelSize / width, (double) maxPixelSize / height);
    int newWidth = (int) (width * scale);
    int newHeight = (int) (height * scale);

    BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    resized.createGraphics().drawImage(image, 0, 0, newWidth, newHeight, null);
    return resized;
  }

  /**
   * Converts ARGB pixel data (as returned by {@link BufferedImage#getRGB}) to RGBA byte order,
   * matching the format expected by Chrome Screen AI's SkBitmap.
   */
  private static byte[] argbToRgba(int[] argbPixels) {
    byte[] rgba = new byte[argbPixels.length * 4];

    for (int i = 0; i < argbPixels.length; i++) {
      int pixel = argbPixels[i];
      int offset = i * 4;
      rgba[offset] = (byte) ((pixel >> 16) & 0xFF); // R
      rgba[offset + 1] = (byte) ((pixel >> 8) & 0xFF); // G
      rgba[offset + 2] = (byte) (pixel & 0xFF); // B
      rgba[offset + 3] = (byte) ((pixel >> 24) & 0xFF); // A
    }

    return rgba;
  }
}
