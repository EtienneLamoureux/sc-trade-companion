# Screenshot Thumbnail Storage

**Date:** 2026-05-30

## Problem

`Screenshot` records store a full-size `BufferedImage` (e.g. 14 MB for a 2560×1440 capture). The repository holds up to 36 of these, totalling ~500 MB. This causes `OutOfMemoryError: Java heap space` after processing a few screenshots.

The image is only ever used to render a 150 px tile in the UI. Storing the full-size image is wasteful.

## Constraint

The screenshot ID is hashed from the full-size image in `StatusTrackingSubmissionFactory` before `ScreenshotFactory` is called. The design must not change that.

## Design

### Where scaling happens

`ScreenshotFactory` scales the incoming `BufferedImage` to a 150 px thumbnail (same constant as the current `ScreenshotTileFactory.MAX_IMAGE_SIZE`) before constructing the `Screenshot` record. The full-size image is never stored.

ID computation happens upstream in `StatusTrackingSubmissionFactory` and is unaffected.

### Changes

**`ScreenshotFactory`**
- Add private `scaleToThumbnail(BufferedImage)` using `Scalr.resize` (library already on classpath).
- Call it in all three `build()` overloads before passing the image to `new Screenshot(...)`.
- Define `static final int THUMBNAIL_SIZE = 150`.

**`ScreenshotTileFactory`**
- Remove `scaleImage()` helper — the stored image is already display-sized.
- Simplify `getOrCreateImage()` to convert directly: `SwingFXUtils.toFXImage(screenshot.image(), null)`.
- Keep `MAX_IMAGE_SIZE = 150` (still used for `ImageView` fit dimensions).
- Keep the LRU `Image` cache (converting `BufferedImage` → JavaFX `Image` still has cost).

**`Screenshot` record** — no changes.

### Memory impact

| | Per screenshot | Repository (36 items) |
|---|---|---|
| Before | ~14 MB | ~500 MB |
| After | ~150 KB | ~5 MB |

Approximately 93× reduction.

## Testing

- `ScreenshotFactoryTest`: add tests verifying that `screenshot.image()` dimensions are ≤ 150 px after `build()`.
- `ScreenshotTileFactoryTest`: remove any tests that exercise the now-deleted `scaleImage()` logic.
