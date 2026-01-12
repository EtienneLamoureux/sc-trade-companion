using System;
using System.IO;
using System.Threading.Tasks;
using Windows.Graphics.Imaging;
using Windows.Media.Ocr;
using Windows.Storage;
using Windows.Storage.Streams;
using System.Text.Json;
using System.Collections.Generic;

namespace WindowsOcrWrapper
{
    class Program
    {
        static async Task Main(string[] args)
        {
            if (args.Length == 0)
            {
                Console.Error.WriteLine("Error: No image path provided.");
                return;
            }

            string imagePath = args[0];

            if (!File.Exists(imagePath))
            {
                Console.Error.WriteLine($"Error: File not found at {imagePath}");
                return;
            }

            try
            {
                var ocrEngine = OcrEngine.TryCreateFromUserProfileLanguages();
                if (ocrEngine == null)
                {
                    Console.Error.WriteLine("Error: Could not create OcrEngine. Ensure a language pack is installed.");
                    return;
                }

                using var softwareBitmap = await LoadImage(imagePath);
                var ocrResult = await ocrEngine.RecognizeAsync(softwareBitmap);

                var output = ConvertToOutput(ocrResult);
                var json = JsonSerializer.Serialize(output, new JsonSerializerOptions { WriteIndented = false });

                Console.WriteLine(json);
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine($"Error: {ex.Message}");
                Console.Error.WriteLine(ex.StackTrace);
            }
        }

        static async Task<SoftwareBitmap> LoadImage(string imagePath)
        {
            var fullPath = Path.GetFullPath(imagePath);

            StorageFile file;
            try
            {
                file = await StorageFile.GetFileFromPathAsync(fullPath);
            }
            catch (Exception ex)
            {
                throw new IOException(
                    $"Failed to access image file at '{fullPath}'. The file may be inaccessible, missing, or you may not have permission to read it.",
                    ex);
            }

            try
            {
                using (IRandomAccessStream stream = await file.OpenAsync(FileAccessMode.Read))
                {
                    BitmapDecoder decoder;
                    try
                    {
                        decoder = await BitmapDecoder.CreateAsync(stream);
                    }
                    catch (Exception ex)
                    {
                        throw new InvalidOperationException(
                            "Failed to decode image. The file may be corrupted or in an unsupported image format.",
                            ex);
                    }

                    // WindowsOCR requires Bgra8
                    return await decoder.GetSoftwareBitmapAsync(BitmapPixelFormat.Bgra8, BitmapAlphaMode.Premultiplied);
                }
            }
            catch (IOException)
            {
                // Re-throw IO-related exceptions without wrapping to preserve their specific messages.
                throw;
            }
            catch (Exception ex)
            {
                throw new IOException("Unexpected error while reading or decoding the image stream.", ex);
            }
        }

        class WordData
        {
            public required string Text { get; set; }
            public double X { get; set; }
            public double Y { get; set; }
            public double Width { get; set; }
            public double Height { get; set; }
        }

        static List<WordData> ConvertToOutput(OcrResult ocrResult)
        {
            var words = new List<WordData>();

            foreach (var line in ocrResult.Lines)
            {
                foreach (var word in line.Words)
                {
                    words.Add(new WordData
                    {
                        Text = word.Text,
                        X = word.BoundingRect.X,
                        Y = word.BoundingRect.Y,
                        Width = word.BoundingRect.Width,
                        Height = word.BoundingRect.Height
                    });
                }
            }
            return words;
        }
    }
}
