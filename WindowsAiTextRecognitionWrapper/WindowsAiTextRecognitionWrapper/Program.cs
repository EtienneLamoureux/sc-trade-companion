#nullable enable

using System;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;
using Microsoft.Windows.AI.Imaging;
using Windows.Graphics.Imaging;
using Windows.Storage;
using Windows.Storage.Streams;

class Program
{
    static async Task<int> Main(string[] args)
    {
        if (args.Length != 1 || !Path.IsPathRooted(args[0]))
        {
            Console.Error.WriteLine("Usage: OCRApp <absolute-image-path>");
            return 1;
        }

        ImageBuffer image = await LoadImageAsync(args[0]);

        var recognizer = await TextRecognizer.CreateAsync();

        var result = recognizer.RecognizeTextFromImage(image);

        var json = new
        {
            FullText = string.Join("\n", result.Lines.Select(l => l.Text)),
            Lines = result.Lines.Select(line => new
            {
                Text = line.Text,
                BoundingBox = Rect(line.BoundingBox),
                Words = line.Words.Select(word => new
                {
                    Text = word.Text,
                    BoundingBox = Rect(word.BoundingBox),
                    Confidence = word.MatchConfidence
                })
            })
        };

        Console.WriteLine(JsonSerializer.Serialize(
            json,
            new JsonSerializerOptions { WriteIndented = true }
        ));

        return 0;
    }

    static async Task<ImageBuffer> LoadImageAsync(string path)
    {
        StorageFile file = await StorageFile.GetFileFromPathAsync(path);
        using IRandomAccessStream stream = await file.OpenAsync(FileAccessMode.Read);

        BitmapDecoder decoder = await BitmapDecoder.CreateAsync(stream);
        SoftwareBitmap bitmap = await decoder.GetSoftwareBitmapAsync(
            BitmapPixelFormat.Bgra8,
            BitmapAlphaMode.Premultiplied
        );

        return ImageBuffer.CreateCopyFromBitmap(bitmap);
    }

    static object Rect(Windows.Foundation.Rect r) => new
    {
        r.X,
        r.Y,
        r.Width,
        r.Height
    };
}
