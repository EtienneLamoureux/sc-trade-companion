#nullable enable

using System;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Graphics.Imaging;
using Windows.Media.Ocr;
using Windows.Storage;
using Windows.Storage.Streams;

internal class Program
{
    static async Task<int> Main(string[] args)
    {
        if (args.Length != 1 || !Path.IsPathRooted(args[0]))
        {
            Console.Error.WriteLine("Usage: WindowsMediaOcrWrapper <absolute-image-path>");
            return 1;
        }

        SoftwareBitmap bitmap = await LoadBitmapAsync(args[0]);

        OcrEngine engine = OcrEngine.TryCreateFromUserProfileLanguages();
        if (engine == null)
        {
            Console.Error.WriteLine("OCR engine not available on this system.");
            return 2;
        }

        OcrResult result = await engine.RecognizeAsync(bitmap);

        var output = new
        {
            FullText = result.Text,
            Lines = result.Lines.Select(line =>
            {
                var words = line.Words.Select(w => new
                {
                    Text = w.Text,
                    BoundingBox = Rect(w.BoundingRect)
                }).ToArray();

                return new
                {
                    Text = line.Text,
                    BoundingBox = Rect(ComputeLineRect(line.Words)),
                    Words = words
                };
            })
        };

        Console.WriteLine(JsonSerializer.Serialize(
            output,
            new JsonSerializerOptions { WriteIndented = true }
        ));

        return 0;
    }

    private static async Task<SoftwareBitmap> LoadBitmapAsync(string path)
    {
        StorageFile file = await StorageFile.GetFileFromPathAsync(path);
        using IRandomAccessStream stream = await file.OpenAsync(FileAccessMode.Read);

        BitmapDecoder decoder = await BitmapDecoder.CreateAsync(stream);
        return await decoder.GetSoftwareBitmapAsync(
            BitmapPixelFormat.Bgra8,
            BitmapAlphaMode.Premultiplied
        );
    }

    private static Rect ComputeLineRect(
        System.Collections.Generic.IReadOnlyList<OcrWord> words)
    {
        if (words.Count == 0)
            return new Rect();

        double x1 = words.Min(w => w.BoundingRect.X);
        double y1 = words.Min(w => w.BoundingRect.Y);
        double x2 = words.Max(w => w.BoundingRect.X + w.BoundingRect.Width);
        double y2 = words.Max(w => w.BoundingRect.Y + w.BoundingRect.Height);

        return new Rect(x1, y1, x2 - x1, y2 - y1);
    }

    private static object Rect(Rect r) => new
    {
        r.X,
        r.Y,
        r.Width,
        r.Height
    };
}
