using Microsoft.Graphics.Imaging;
using Microsoft.Windows.AI.Imaging;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using Windows.Graphics.Imaging;
using Windows.Storage;
using Windows.Storage.Streams;

if (args.Length == 0)
{
    Console.Error.WriteLine("Usage: WindowsAiTextRecognitionWrapper <absolute-image-path>");
    return 1;
}

string imagePath = args[0];

if (!File.Exists(imagePath))
{
    Console.Error.WriteLine($"File not found: {imagePath}");
    return 1;
}

SoftwareBitmap bitmap = await LoadBitmapAsync(imagePath);
TextRecognizer textRecognizer = await EnsureModelIsReady();
ImageBuffer imageBuffer = ImageBuffer.CreateBufferAttachedToBitmap(bitmap);
RecognizedText recognizedText = textRecognizer.RecognizeTextFromImage(imageBuffer);

var words = new List<WordResult>();

foreach (var line in recognizedText.Lines)
{
    foreach (var word in line.Words)
    {
        var bounds = word.BoundingBox;
        words.Add(new WordResult
        {
            Text = word.Text,
            Confidence = word.Confidence,
            BoundingBox = new BoundingBoxResult
            {
                TopLeft     = new PointResult { X = bounds.TopLeft.X,     Y = bounds.TopLeft.Y },
                TopRight    = new PointResult { X = bounds.TopRight.X,    Y = bounds.TopRight.Y },
                BottomRight = new PointResult { X = bounds.BottomRight.X, Y = bounds.BottomRight.Y },
                BottomLeft  = new PointResult { X = bounds.BottomLeft.X,  Y = bounds.BottomLeft.Y },
            }
        });
    }
}

var result = new RecognitionResult
{
    FullText = recognizedText.Text,
    Words = words
};

string json = JsonSerializer.Serialize(result, new JsonSerializerOptions { WriteIndented = true });
Console.WriteLine(json);
return 0;

// --- Local functions ---

static async Task<SoftwareBitmap> LoadBitmapAsync(string path)
{
    StorageFile file = await StorageFile.GetFileFromPathAsync(path);
    using IRandomAccessStream stream = await file.OpenAsync(FileAccessMode.Read);
    BitmapDecoder decoder = await BitmapDecoder.CreateAsync(stream);
    return await decoder.GetSoftwareBitmapAsync(BitmapPixelFormat.Bgra8, BitmapAlphaMode.Premultiplied);
}

static async Task<TextRecognizer> EnsureModelIsReady()
{
    if (TextRecognizer.GetReadyState() == AIFeatureReadyState.NotReady)
    {
        var loadResult = await TextRecognizer.EnsureReadyAsync();
        if (loadResult.Status != AIFeatureReadyResultState.Success)
        {
            throw new Exception(loadResult.ExtendedError().Message);
        }
    }
    return await TextRecognizer.CreateAsync();
}

// --- DTOs ---

class Recognition
