# Perspective Correction Usage Examples

This document provides examples of how to use the new perspective correction and largest rectangle detection features added to the image processing pipeline.

## Overview

Three new `ImageManipulation` implementations have been added:
- `PerspectiveCorrection`: Detects and corrects perspective distortion in images containing quadrilateral shapes
- `LargestRectangleCrop`: Crops an image to its largest rectangular content region (useful for removing black borders)
- `PerspectiveCorrectionAndCrop`: Combined manipulation that applies perspective correction and crops to the transformed rectangle in one step (recommended)

## Basic Usage

### Using PerspectiveCorrectionAndCrop (Recommended)

```java
import tools.sctrade.companion.domain.image.manipulations.PerspectiveCorrectionAndCrop;
import java.awt.image.BufferedImage;

// Apply perspective correction and crop to the transformed rectangle
PerspectiveCorrectionAndCrop manipulation = new PerspectiveCorrectionAndCrop();
BufferedImage processedImage = manipulation.manipulate(originalImage);
```

This is the recommended approach as it:
1. Detects the largest quadrilateral in the image
2. Applies perspective transformation to straighten it
3. Crops to the exact rectangle used for transformation (not black border detection)

### Using PerspectiveCorrection Alone

```java
import tools.sctrade.companion.domain.image.manipulations.PerspectiveCorrection;
import java.awt.image.BufferedImage;

// Apply perspective correction to an image
PerspectiveCorrection correction = new PerspectiveCorrection();
BufferedImage correctedImage = correction.manipulate(originalImage);
```

### Using LargestRectangleCrop

```java
import tools.sctrade.companion.domain.image.manipulations.LargestRectangleCrop;
import java.awt.image.BufferedImage;

// Crop to the largest rectangle in the image
LargestRectangleCrop crop = new LargestRectangleCrop();
BufferedImage croppedImage = crop.manipulate(originalImage);
```

### Advanced: Using the API Directly

```java
import tools.sctrade.companion.utils.ImageUtil;
import tools.sctrade.companion.utils.ImageUtil.PerspectiveCorrectionResult;
import java.awt.image.BufferedImage;

// Get both the transformed image and the rectangle used
PerspectiveCorrectionResult result = ImageUtil.applyPerspectiveCorrection(originalImage);
BufferedImage transformedImage = result.image();
Rectangle transformRect = result.rectangle(); // null if no quadrilateral found

// Use the same rectangle for cropping
if (transformRect != null) {
  BufferedImage croppedImage = ImageUtil.cropToLargestRectangle(transformedImage, transformRect);
}
```

## Adding to the Processing Pipeline

To add these manipulations to the image processing pipeline, modify `AppConfig.java`:

```java
@Bean("ScreenPrinter")
public ScreenPrinter buildScreenPrinter(
    @Qualifier("CommodityService") CommodityService commodityService,
    ImageWriter<Optional<Path>> imageWriter, SoundUtil soundPlayer,
    NotificationService notificationService, SettingRepository settings) {
  List<ImageManipulation> postprocessingManipulations = new ArrayList<>();
  
  // Add combined perspective correction and crop (recommended)
  postprocessingManipulations.add(new PerspectiveCorrectionAndCrop());
  postprocessingManipulations.add(new UpscaleTo4k());

  return new ScreenPrinter(Arrays.asList(commodityService), postprocessingManipulations,
      imageWriter, soundPlayer, notificationService, settings);
}
```

Similarly, for OCR processing in `CommoditySubmissionFactory`:

```java
Ocr ocr = new WindowsOcr(
    List.of(
        new PerspectiveCorrectionAndCrop(),  // Combined correction and crop
        new UpscaleTo4k(), 
        new InvertColors(), 
        new ConvertToEqualizedGreyscale()
    ),
    diskImageWriter, new ProcessRunner(), notificationService);
```

## How It Works

### Perspective Correction

1. **Edge Detection**: Converts a copy of the image to grayscale and applies Gaussian blur (recommended for noise reduction), then Canny edge detection
2. **Contour Finding**: Finds all contours in the edge-detected image
3. **Quadrilateral Detection**: Identifies the largest four-sided contour
4. **Perspective Transform**: Applies a perspective transformation (homography) to the **original color image** to straighten the quadrilateral
5. **Returns Record**: Returns both the transformed image and the rectangle dimensions used for transformation
6. **Fallback**: If no suitable quadrilateral is found, returns the original image with null rectangle

**Note**: Grayscale conversion and blurring are only used for edge detection, not for the actual transformation. This preserves the original image quality.

### Largest Rectangle Crop

**With Rectangle Parameter (Used by PerspectiveCorrectionAndCrop):**
- Directly crops to the provided rectangle (from perspective transformation)
- No additional detection or grayscale conversion needed

**Without Rectangle Parameter (Auto-detect mode):**
1. **Thresholding**: Converts the image to grayscale and applies binary thresholding
2. **Contour Detection**: Finds all external contours
3. **Bounding Box**: Identifies the bounding box of the largest contour
4. **Cropping**: Crops the image to that bounding box
5. **Fallback**: If no suitable rectangle is found, returns the original image unchanged

### PerspectiveCorrectionAndCrop (Combined)

1. Applies perspective correction to detect and straighten the largest quadrilateral
2. Uses the **same rectangle** from the perspective transform for cropping
3. No separate black border detection needed
4. More efficient and accurate than running separate steps

## Configuration Constants

The perspective correction algorithm uses two tunable constants in `ImageUtil.java`:

```java
private static final double MIN_CONTOUR_AREA = 1000.0;  // Minimum area for contour detection
private static final double APPROX_POLY_EPSILON_FACTOR = 0.02;  // Polygon approximation precision
```

Adjust these values if you need to fine-tune the detection sensitivity.

## Benefits for SC Trade Companion

- **Better OCR Accuracy**: Correcting perspective distortion before OCR improves text recognition
- **Flexible Positioning**: Players don't need to be perfectly aligned with commodity kiosk screens
- **Cleaner Screenshots**: Automatic cropping removes unnecessary borders
- **Consistent Processing**: All images are normalized to a standard perspective
- **Preserves Quality**: Original color image is used for transformation, not grayscale

## Testing

Comprehensive unit tests are provided in:
- `ImageUtilPerspectiveTest.java`: Tests for the core utility methods
- `PerspectiveCorrectionTest.java`: Tests for the PerspectiveCorrection manipulation
- `PerspectiveCorrectionAndCropTest.java`: Tests for the combined manipulation
- `LargestRectangleCropTest.java`: Tests for the cropping manipulation

Run tests with:
```bash
./gradlew test --tests ImageUtilPerspectiveTest
./gradlew test --tests PerspectiveCorrectionTest
./gradlew test --tests PerspectiveCorrectionAndCropTest
./gradlew test --tests LargestRectangleCropTest
```
