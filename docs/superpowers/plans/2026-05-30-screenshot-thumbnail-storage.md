# Screenshot Thumbnail Storage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Store only a 150 px thumbnail in `Screenshot` records (instead of the full-size `BufferedImage`) to prevent `OutOfMemoryError`, and raise the JVM heap ceiling from 512 MB to 2024 MB in all launcher scripts.

**Architecture:** `ScreenshotFactory` scales every incoming image to a 150 px thumbnail before constructing the `Screenshot` record. `Screenshot`'s redundant defensive-copy constructor is removed. `ScreenshotTileFactory`'s now-dead `scaleImage()` helper is deleted and `getOrCreateImage()` is simplified to a direct JavaFX conversion.

**Tech Stack:** Java 21, `imgscalr` (`org.imgscalr.Scalr` — already on classpath), JUnit 5, Mockito.

---

## File Map

| Action | File |
|---|---|
| Modify | `src/main/java/tools/sctrade/companion/gui/screenshot/Screenshot.java` |
| Modify | `src/main/java/tools/sctrade/companion/gui/screenshot/ScreenshotFactory.java` |
| Modify | `src/main/java/tools/sctrade/companion/gui/screenshot/ScreenshotTileFactory.java` |
| Modify | `src/test/java/tools/sctrade/companion/gui/screenshot/ScreenshotTest.java` |
| Modify | `src/test/java/tools/sctrade/companion/gui/screenshot/ScreenshotFactoryTest.java` |
| Modify | `scripts/sc-trade-companion.bat` |
| Modify | `scripts/sc-trade-companion-admin.bat` |
| Modify | `scripts/sc-trade-companion-debug.bat` |

---

## Task 1: Remove the defensive copy from `Screenshot` and raise the heap limit

**Files:**
- Modify: `src/main/java/tools/sctrade/companion/gui/screenshot/Screenshot.java`
- Modify: `src/test/java/tools/sctrade/companion/gui/screenshot/ScreenshotTest.java`
- Modify: `scripts/sc-trade-companion.bat`
- Modify: `scripts/sc-trade-companion-admin.bat`
- Modify: `scripts/sc-trade-companion-debug.bat`

The compact constructor copies the `BufferedImage` on every `Screenshot` construction (including inside `updateUsing()`), causing 3× copies per processed screenshot. Since `Screenshot` is an immutable record and callers never mutate the image after passing it, the copy is wasteful. The test that asserts the copy behaviour must be removed.

- [ ] **Step 1: Remove the compact constructor and its imports from `Screenshot.java`**

Replace the entire file with:

```java
package tools.sctrade.companion.gui.screenshot;

import java.awt.image.BufferedImage;

/**
 * Immutable snapshot of a screenshot submitted for processing.
 *
 * @param id Unique identifier.
 * @param image Thumbnail image data (≤ 150 px), or {@code null} if not available.
 * @param location In-game location read from the screenshot, or {@code null} if not yet determined.
 * @param status Current processing status.
 * @param error Human-readable error message, or {@code null} if there is none.
 * @param content Raw OCR content extracted from the image, or {@code null} if not yet extracted.
 * @param type Recognised kiosk type of the screenshot.
 */
public record Screenshot(String id, BufferedImage image, String location, ScreenshotStatus status,
    String error, String content, ScreenshotType type) {

  /**
   * Returns a new {@link Screenshot} that merges {@code update} into this record.
   *
   * <p>
   * Each field of {@code update} replaces the corresponding field of this record when the update
   * value is non-{@code null}; otherwise the existing value is kept. The {@code id} must match.
   *
   * @param update Partial update to apply. Must carry the same {@code id} as this record.
   * @return A fresh {@link Screenshot} with the merged field values.
   * @throws IllegalArgumentException if {@code update.id()} differs from this record's {@code id}.
   */
  public Screenshot updateUsing(Screenshot update) {
    if (!this.id.equals(update.id())) {
      throw new IllegalArgumentException(
          "Cannot update screenshot '%s' using screenshot with different id '%s'".formatted(this.id,
              update.id()));
    }

    return new Screenshot(this.id, update.image() != null ? update.image() : this.image,
        update.location() != null ? update.location() : this.location,
        update.status() != null ? update.status() : this.status,
        update.error() != null ? update.error() : this.error,
        update.content() != null ? update.content() : this.content,
        update.type() != null ? update.type() : this.type);
  }
}
```

- [ ] **Step 2: Remove the defensive-copy test from `ScreenshotTest.java`**

Delete the entire `whenMutatingOriginalImageAfterConstruction_thenStoredImageIsUnchanged` test method (lines 16–26). Leave the remaining four tests untouched. Also remove the now-unused `BufferedImage` import if no other test uses it.

Final file:

```java
package tools.sctrade.companion.gui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ScreenshotTest {

  private static final Screenshot BASE = new Screenshot("id-1", null, "Orison",
      ScreenshotStatus.QUEUED, null, null, ScreenshotType.COMMODITY_KIOSK);

  @Test
  void givenUpdateWithNonNullField_whenUpdateUsing_thenFieldIsReplaced() {
    var update = new Screenshot("id-1", null, null, ScreenshotStatus.PROCESSING, null, null, null);

    Screenshot result = BASE.updateUsing(update);

    assertEquals(ScreenshotStatus.PROCESSING, result.status());
  }

  @Test
  void givenUpdateWithNullField_whenUpdateUsing_thenOriginalFieldIsKept() {
    var update = new Screenshot("id-1", null, null, ScreenshotStatus.PROCESSING, null, null, null);

    Screenshot result = BASE.updateUsing(update);

    assertEquals("Orison", result.location());
  }

  @Test
  void givenUpdateWithAllNullOptionalFields_whenUpdateUsing_thenAllOriginalFieldsAreKept() {
    var update = new Screenshot("id-1", null, null, null, null, null, null);

    Screenshot result = BASE.updateUsing(update);

    assertEquals(BASE.location(), result.location());
    assertEquals(BASE.status(), result.status());
    assertEquals(BASE.type(), result.type());
    assertNull(result.error());
    assertNull(result.content());
  }

  @Test
  void givenUpdateWithDifferentId_whenUpdateUsing_thenThrows() {
    var update = new Screenshot("id-2", null, null, ScreenshotStatus.SUCCESS, null, null, null);

    assertThrows(IllegalArgumentException.class, () -> BASE.updateUsing(update));
  }
}
```

- [ ] **Step 3: Update bat scripts to `-Xmx2024m`**

`scripts/sc-trade-companion.bat` — change the single line:
```bat
start bin\jre\bin\javaw.exe -Xmx2024m -Djava.net.preferIPv4Stack=true -jar bin\sc-trade-companion.jar
```

`scripts/sc-trade-companion-admin.bat` — last line becomes:
```bat
start bin\jre\bin\javaw.exe -Xmx2024m -Djava.net.preferIPv4Stack=true -jar bin\sc-trade-companion.jar
```

`scripts/sc-trade-companion-debug.bat` — update both the `echo` line and the execution line:
```bat
echo bin\jre\bin\java.exe -Xmx2024m -Djava.net.preferIPv4Stack=true -cp !CLASSPATH! tools.sctrade.companion.CompanionApplication
...
bin\jre\bin\java.exe -Xmx2024m -Djava.net.preferIPv4Stack=true -cp !CLASSPATH! tools.sctrade.companion.CompanionApplication
```

- [ ] **Step 4: Format and build**

```
cd C:\Prog\java\sc-trade-companion
.\gradlew spotlessApply --no-daemon
.\gradlew clean build --no-daemon
```

Expected: BUILD SUCCESSFUL. All existing tests pass.

- [ ] **Step 5: Commit**

```
git add src/main/java/tools/sctrade/companion/gui/screenshot/Screenshot.java
git add src/test/java/tools/sctrade/companion/gui/screenshot/ScreenshotTest.java
git add scripts/sc-trade-companion.bat scripts/sc-trade-companion-admin.bat scripts/sc-trade-companion-debug.bat
git commit -m "perf: remove defensive BufferedImage copy from Screenshot and raise heap to 2024m"
```

---

## Task 2: Scale to thumbnail in `ScreenshotFactory`

**Files:**
- Modify: `src/main/java/tools/sctrade/companion/gui/screenshot/ScreenshotFactory.java`
- Modify: `src/test/java/tools/sctrade/companion/gui/screenshot/ScreenshotFactoryTest.java`

`ScreenshotFactory` is the single point of `Screenshot` construction, making it the right place to enforce the thumbnail contract. It already lives in the `gui.screenshot` package alongside `ScreenshotTileFactory`, which uses `Scalr` — the same library is available here.

- [ ] **Step 1: Write the failing test**

Add to `ScreenshotFactoryTest.java` (inside the class, after existing tests):

```java
@Test
void givenOversizeImage_whenBuildingProcessingScreenshot_thenStoredImageIsScaledToThumbnail() {
  BufferedImage large = new BufferedImage(500, 400, BufferedImage.TYPE_INT_RGB);

  Screenshot screenshot = screenshotFactory.build("id", large, ScreenshotType.COMMODITY_KIOSK);

  assertTrue(screenshot.image().getWidth() <= ScreenshotFactory.THUMBNAIL_SIZE);
  assertTrue(screenshot.image().getHeight() <= ScreenshotFactory.THUMBNAIL_SIZE);
}
```

Also add `import static org.junit.jupiter.api.Assertions.assertTrue;` to the imports.

- [ ] **Step 2: Run the test to verify it fails**

```
.\gradlew test --tests "tools.sctrade.companion.gui.screenshot.ScreenshotFactoryTest.givenOversizeImage_whenBuildingProcessingScreenshot_thenStoredImageIsScaledToThumbnail" --no-daemon
```

Expected: FAIL — `THUMBNAIL_SIZE` does not exist yet.

- [ ] **Step 3: Implement thumbnail scaling in `ScreenshotFactory`**

Replace the entire file with:

```java
package tools.sctrade.companion.gui.screenshot;

import java.awt.image.BufferedImage;
import org.imgscalr.Scalr;
import tools.sctrade.companion.domain.commodity.CommodityListing;
import tools.sctrade.companion.domain.commodity.CommoditySubmission;
import tools.sctrade.companion.domain.item.ItemListing;
import tools.sctrade.companion.domain.item.ItemSubmission;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * Builds screenshot records for processing lifecycle events.
 *
 * <p>
 * All {@code build} overloads scale the provided image to a {@value #THUMBNAIL_SIZE} px thumbnail
 * before storing it in the {@link Screenshot} record.
 */
public class ScreenshotFactory {

  static final int THUMBNAIL_SIZE = 150;

  /**
   * Builds a processing-status screenshot record.
   *
   * @param id screenshot id
   * @param image original screenshot image
   * @param type screenshot type
   * @return processing-status screenshot record
   */
  public Screenshot build(String id, BufferedImage image, ScreenshotType type) {
    return new Screenshot(id, scaleToThumbnail(image), null, ScreenshotStatus.PROCESSING, null,
        null, type);
  }

  /**
   * Builds a success-status screenshot record.
   *
   * @param id screenshot id
   * @param image original screenshot image
   * @param submission decoded submission object
   * @param type screenshot type
   * @return success-status screenshot record
   */
  public Screenshot build(String id, BufferedImage image, Object submission, ScreenshotType type) {
    if (submission instanceof ItemSubmission itemSubmission) {
      return buildFromItemSubmission(id, image, itemSubmission, type);
    }

    return new Screenshot(id, scaleToThumbnail(image), extractLocation(submission),
        ScreenshotStatus.SUCCESS, null, extractContent(submission), type);
  }

  /**
   * Builds an error-status screenshot record.
   *
   * @param id screenshot id
   * @param image original screenshot image
   * @param exception exception raised while processing
   * @param type screenshot type
   * @return error-status screenshot record
   */
  public Screenshot build(String id, BufferedImage image, RuntimeException exception,
      ScreenshotType type) {
    return new Screenshot(id, scaleToThumbnail(image), null, ScreenshotStatus.ERROR,
        exception.getMessage(), null, type);
  }

  private Screenshot buildFromItemSubmission(String id, BufferedImage image,
      ItemSubmission itemSubmission, ScreenshotType type) {
    if (itemSubmission.isEmpty() || hasMissingLocation(itemSubmission)) {
      return new Screenshot(id, scaleToThumbnail(image), null, ScreenshotStatus.ERROR,
          LocalizationUtil.get("warnNoLocation"), null, type);
    }

    if (hasMissingShop(itemSubmission)) {
      return new Screenshot(id, scaleToThumbnail(image), null, ScreenshotStatus.ERROR,
          LocalizationUtil.get("warnNoShop"), null, type);
    }

    return new Screenshot(id, scaleToThumbnail(image), extractLocation(itemSubmission),
        ScreenshotStatus.SUCCESS, null, extractContent(itemSubmission), type);
  }

  private BufferedImage scaleToThumbnail(BufferedImage image) {
    if (image == null) {
      return null;
    }
    if (image.getWidth() <= THUMBNAIL_SIZE && image.getHeight() <= THUMBNAIL_SIZE) {
      return image;
    }
    return Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, THUMBNAIL_SIZE,
        THUMBNAIL_SIZE);
  }

  private boolean hasMissingLocation(ItemSubmission itemSubmission) {
    return itemSubmission.getListings().stream().map(ItemListing::location)
        .anyMatch(location -> location == null || location.isBlank());
  }

  private boolean hasMissingShop(ItemSubmission itemSubmission) {
    return itemSubmission.getListings().stream().map(ItemListing::shop)
        .anyMatch(shop -> shop == null || shop.isBlank());
  }

  private String extractLocation(Object submission) {
    if (submission instanceof CommoditySubmission commoditySubmission) {
      return commoditySubmission.getListings().stream().map(CommodityListing::location)
          .filter(l -> l != null).findFirst().orElse(null);
    }

    if (submission instanceof ItemSubmission itemSubmission) {
      return itemSubmission.getListings().stream().map(ItemListing::location).filter(l -> l != null)
          .findFirst().orElse(null);
    }

    return null;
  }

  private String extractContent(Object submission) {
    if (submission instanceof CommoditySubmission commoditySubmission) {
      return LocalizationUtil.get("infoScreenshotListingsRead")
          .formatted(commoditySubmission.getListings().size());
    }

    if (submission instanceof ItemSubmission itemSubmission) {
      return LocalizationUtil.get("infoScreenshotListingsRead")
          .formatted(itemSubmission.getListings().size());
    }

    return null;
  }
}
```

- [ ] **Step 4: Run the test to verify it passes**

```
.\gradlew test --tests "tools.sctrade.companion.gui.screenshot.ScreenshotFactoryTest.givenOversizeImage_whenBuildingProcessingScreenshot_thenStoredImageIsScaledToThumbnail" --no-daemon
```

Expected: PASS.

- [ ] **Step 5: Format and run all tests**

```
.\gradlew spotlessApply --no-daemon
.\gradlew clean build --no-daemon
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```
git add src/main/java/tools/sctrade/companion/gui/screenshot/ScreenshotFactory.java
git add src/test/java/tools/sctrade/companion/gui/screenshot/ScreenshotFactoryTest.java
git commit -m "perf: scale screenshot images to 150px thumbnail in ScreenshotFactory"
```

---

## Task 3: Remove dead scaling code from `ScreenshotTileFactory`

**Files:**
- Modify: `src/main/java/tools/sctrade/companion/gui/screenshot/ScreenshotTileFactory.java`

`scaleImage()` is now dead code — images arriving from the repository are already ≤ 150 px. `getOrCreateImage()` can convert directly to a JavaFX `Image` without any intermediate scaling step.

- [ ] **Step 1: Simplify `ScreenshotTileFactory`**

Replace the entire file with:

```java
package tools.sctrade.companion.gui.screenshot;

import atlantafx.base.controls.Tile;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tools.sctrade.companion.gui.TileFactory;

/**
 * Factory for building screenshot tiles.
 */
public class ScreenshotTileFactory implements TileFactory<Screenshot> {
  private static final int MAX_IMAGE_SIZE = 150;

  private final int imageCacheCapacity;
  private final LinkedHashMap<String, Image> imageByScreenshotId;

  /**
   * Constructor.
   *
   * @param screenshotRepository screenshot repository used to derive cache capacity
   */
  public ScreenshotTileFactory(ScreenshotRepository screenshotRepository) {
    this.imageCacheCapacity = screenshotRepository.getCapacity();
    this.imageByScreenshotId = new LinkedHashMap<>(imageCacheCapacity, 0.75f, true) {
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
        return size() > imageCacheCapacity;
      }
    };
  }

  @Override
  public Tile build(Screenshot screenshot) {
    Tile tile = new Tile(screenshot.type().label(), createDescription(screenshot));
    tile.getStyleClass().add("screenshot-tile");
    tile.setGraphic(createGraphic(screenshot));
    return tile;
  }

  private ImageView createGraphic(Screenshot screenshot) {
    if (screenshot.image() == null) {
      return null;
    }

    ImageView imageView = new ImageView(getOrCreateImage(screenshot));
    imageView.setFitWidth(MAX_IMAGE_SIZE);
    imageView.setFitHeight(MAX_IMAGE_SIZE);
    imageView.setPreserveRatio(true);
    return imageView;
  }

  private String createDescription(Screenshot screenshot) {
    ScreenshotStatus status = screenshot.status();
    String statusText = switch (status) {
      case SUCCESS -> screenshot.content() != null ? screenshot.content() : status.defaultText();
      case ERROR -> screenshot.error() != null ? screenshot.error() : status.defaultText();
      default -> status.defaultText();
    };

    String glyph = status.glyph();

    return "[span class=\"screenshot-tile-status " + status.styleClass() + "\"]" + glyph + " "
        + sanitizeBbCodeText(statusText) + "[/span]";
  }

  private String sanitizeBbCodeText(String text) {
    return text.replace("[", "［").replace("]", "］");
  }

  private Image getOrCreateImage(Screenshot screenshot) {
    String id = screenshot.id();
    Image cached = imageByScreenshotId.get(id);
    if (cached != null) {
      return cached;
    }

    Image image = SwingFXUtils.toFXImage(screenshot.image(), null);
    imageByScreenshotId.put(id, image);
    return image;
  }
}
```

- [ ] **Step 2: Format and run all tests**

```
.\gradlew spotlessApply --no-daemon
.\gradlew clean build --no-daemon
```

Expected: BUILD SUCCESSFUL. All existing `ScreenshotTileFactoryTest` tests pass unchanged.

- [ ] **Step 3: Commit**

```
git add src/main/java/tools/sctrade/companion/gui/screenshot/ScreenshotTileFactory.java
git commit -m "perf: remove dead scaleImage from ScreenshotTileFactory"
```
