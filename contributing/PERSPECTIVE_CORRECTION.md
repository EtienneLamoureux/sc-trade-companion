# Perspective Correction Usage Examples

This document provides examples of how to use the new perspective correction and largest rectangle detection features added to the image processing pipeline.

## Overview

Two new `ImageManipulation` implementations have been added:
- `PerspectiveCorrection`: Detects and corrects perspective distortion in images containing quadrilateral shapes
- `LargestRectangleCrop`: Crops an image to its largest rectangular content region (useful for removing black borders)

## Basic Usage

### Using PerspectiveCorrection

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

## Adding to the Processing Pipeline

To add these manipulations to the image processing pipeline, modify `AppConfig.java`:

```java
@Bean("ScreenPrinter")
public ScreenPrinter buildScreenPrinter(
    @Qualifier("CommodityService") CommodityService commodityService,
    ImageWriter<Optional<Path>> imageWriter, SoundUtil soundPlayer,
    NotificationService notificationService, SettingRepository settings) {
  List<ImageManipulation> postprocessingManipulations = new ArrayList<>();
  
  // Add perspective correction before other processing
  postprocessingManipulations.add(new PerspectiveCorrection());
  postprocessingManipulations.add(new LargestRectangleCrop());
  postprocessingManipulations.add(new UpscaleTo4k());

  return new ScreenPrinter(Arrays.asList(commodityService), postprocessingManipulations,
      imageWriter, soundPlayer, notificationService, settings);
}
```

Similarly, for OCR processing in `CommoditySubmissionFactory`:

```java
Ocr ocr = new WindowsOcr(
    List.of(
        new PerspectiveCorrection(),
        new LargestRectangleCrop(),
        new UpscaleTo4k(), 
        new InvertColors(), 
        new ConvertToEqualizedGreyscale()
    ),
    diskImageWriter, new ProcessRunner(), notificationService);
```

## How It Works

### Perspective Correction

1. **Edge Detection**: Converts the image to grayscale and applies Canny edge detection
2. **Contour Finding**: Finds all contours in the edge-detected image
3. **Quadrilateral Detection**: Identifies the largest four-sided contour
4. **Perspective Transform**: Applies a perspective transformation (homography) to straighten the quadrilateral
5. **Fallback**: If no suitable quadrilateral is found, returns the original image unchanged

### Largest Rectangle Crop

1. **Thresholding**: Converts the image to grayscale and applies binary thresholding
2. **Contour Detection**: Finds all external contours
3. **Bounding Box**: Identifies the bounding box of the largest contour
4. **Cropping**: Crops the image to that bounding box
5. **Fallback**: If no suitable rectangle is found, returns the original image unchanged

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

## Testing

Comprehensive unit tests are provided in:
- `ImageUtilPerspectiveTest.java`: Tests for the core utility methods
- `PerspectiveCorrectionTest.java`: Tests for the manipulation class
- `LargestRectangleCropTest.java`: Tests for the cropping manipulation

Run tests with:
```bash
./gradlew test --tests ImageUtilPerspectiveTest
./gradlew test --tests PerspectiveCorrectionTest
./gradlew test --tests LargestRectangleCropTest
```
