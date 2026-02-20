using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.Runtime.InteropServices;

#region Native structs

[StructLayout(LayoutKind.Sequential, Pack = 1)]
struct Img
{
    public int t;
    public int col;
    public int row;
    public int unk;
    public long step;
    public long data_ptr;
}

[StructLayout(LayoutKind.Sequential, Pack = 1)]
struct OcrBoundingBox
{
    public int left;
    public int top;
    public int right;
    public int bottom;
}

#endregion

#region PInvoke

static class OneOcr
{
    const string DLL = "oneocr.dll";

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long CreateOcrInitOptions(out IntPtr ctx);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long OcrInitOptionsSetUseModelDelayLoad(IntPtr ctx, byte flag);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long CreateOcrPipeline(
        IntPtr modelPath,
        IntPtr key,
        IntPtr ctx,
        out IntPtr pipeline);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long CreateOcrProcessOptions(out IntPtr opt);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long OcrProcessOptionsSetMaxRecognitionLineCount(IntPtr opt, long count);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long RunOcrPipeline(
        IntPtr pipeline, ref Img img, IntPtr opt, out IntPtr instance);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long GetOcrLineCount(IntPtr instance, out long count);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long GetOcrLine(IntPtr instance, long index, out IntPtr line);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long GetOcrLineContent(IntPtr line, out IntPtr textPtr);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long GetOcrLineBoundingBox(IntPtr line, out OcrBoundingBox box);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long GetOcrLineWordCount(IntPtr line, out long count);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long GetOcrWord(IntPtr line, long index, out IntPtr word);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long GetOcrWordContent(IntPtr word, out IntPtr textPtr);

    [DllImport(DLL, CallingConvention = CallingConvention.Cdecl)]
    public static extern long GetOcrWordBoundingBox(IntPtr word, out OcrBoundingBox box);
}

#endregion

class Program
{
    static void Main(string[] args)
    {
        if (args.Length < 1)
        {
            Console.WriteLine("Usage: OneOcrDemo.exe <image.png|bmp|jpg>");
            return;
        }

        using Bitmap bmp = new Bitmap(args[0]);
        using Bitmap bgra = bmp.Clone(new Rectangle(0, 0, bmp.Width, bmp.Height), PixelFormat.Format32bppArgb);

        int width = bgra.Width;
        int height = bgra.Height;

        BitmapData bmpData = bgra.LockBits(
            new Rectangle(0, 0, width, height),
            ImageLockMode.ReadOnly,
            PixelFormat.Format32bppArgb);

        try
        {
            Img img = new Img
            {
                t = 3,
                col = width,
                row = height,
                unk = 0,
                step = bmpData.Stride,
                data_ptr = bmpData.Scan0.ToInt64()
            };

            RunOcr(img);
        }
        finally
        {
            bgra.UnlockBits(bmpData);
        }
    }

    static byte[] StringToAnsiBytes(string s)
    {
        // null-terminated ANSI bytes, matching const char* in C++
        var bytes = System.Text.Encoding.Default.GetBytes(s);
        var result = new byte[bytes.Length + 1];
        Buffer.BlockCopy(bytes, 0, result, 0, bytes.Length);
        return result;
    }

    static void RunOcr(Img img)
    {
        IntPtr ctx, pipeline, opt, instance;

        long res;

        res = OneOcr.CreateOcrInitOptions(out ctx);
        if (res != 0) { Console.Error.WriteLine($"CreateOcrInitOptions failed: {res}"); return; }

        res = OneOcr.OcrInitOptionsSetUseModelDelayLoad(ctx, 0);
        if (res != 0) { Console.Error.WriteLine($"OcrInitOptionsSetUseModelDelayLoad failed: {res}"); return; }

        string model = "oneocr.onemodel";
        string key = "kj)TGtrK>f]b[Piow.gU+nC@s\"\"\"\"\"\"4";

        // Pin byte arrays so the native side gets stable const char* pointers
        byte[] modelBytes = StringToAnsiBytes(model);
        byte[] keyBytes = StringToAnsiBytes(key);

        GCHandle modelHandle = GCHandle.Alloc(modelBytes, GCHandleType.Pinned);
        GCHandle keyHandle = GCHandle.Alloc(keyBytes, GCHandleType.Pinned);

        try
        {
            IntPtr modelPtr = modelHandle.AddrOfPinnedObject();
            IntPtr keyPtr = keyHandle.AddrOfPinnedObject();

            res = OneOcr.CreateOcrPipeline(modelPtr, keyPtr, ctx, out pipeline);
            if (res != 0) { Console.Error.WriteLine($"CreateOcrPipeline failed: {res}"); return; }

            Console.WriteLine("OCR model loaded...");

            res = OneOcr.CreateOcrProcessOptions(out opt);
            if (res != 0) { Console.Error.WriteLine($"CreateOcrProcessOptions failed: {res}"); return; }

            res = OneOcr.OcrProcessOptionsSetMaxRecognitionLineCount(opt, 1000);
            if (res != 0) { Console.Error.WriteLine($"OcrProcessOptionsSetMaxRecognitionLineCount failed: {res}"); return; }

            res = OneOcr.RunOcrPipeline(pipeline, ref img, opt, out instance);
            if (res != 0) { Console.Error.WriteLine($"RunOcrPipeline failed: {res}"); return; }
        }
        finally
        {
            modelHandle.Free();
            keyHandle.Free();
        }

        Console.WriteLine("Running ocr pipeline...");

        OneOcr.GetOcrLineCount(instance, out long lines);
        Console.WriteLine($"Recognize {lines} lines");

        for (long i = 0; i < lines; i++)
        {
            OneOcr.GetOcrLine(instance, i, out IntPtr line);
            if (line == IntPtr.Zero) continue;

            OneOcr.GetOcrLineContent(line, out IntPtr textPtr);
            string text = Marshal.PtrToStringAnsi(textPtr);

            OneOcr.GetOcrLineBoundingBox(line, out OcrBoundingBox box);

            Console.WriteLine(
                $"{i:D2}: [{box.left},{box.top},{box.right},{box.bottom}] {text}");

            OneOcr.GetOcrLineWordCount(line, out long wc);
            for (long j = 0; j < wc; j++)
            {
                OneOcr.GetOcrWord(line, j, out IntPtr word);
                OneOcr.GetOcrWordContent(word, out IntPtr wptr);
                OneOcr.GetOcrWordBoundingBox(word, out OcrBoundingBox wbox);

                string wtext = Marshal.PtrToStringAnsi(wptr);
                Console.WriteLine(
                    $"    [{wbox.left},{wbox.top},{wbox.right},{wbox.bottom}] {wtext}");
            }
        }
    }
}
