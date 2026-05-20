package tools.sctrade.companion.domain.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScreenshotRepositoryTest {

  private static final Screenshot SCREENSHOT_A = new Screenshot("id-a", null, "Orison",
      ScreenshotStatus.QUEUED, null, null, ScreenshotType.COMMODITY_KIOSK);
  private static final Screenshot SCREENSHOT_B = new Screenshot("id-b", null, "Area18",
      ScreenshotStatus.QUEUED, null, null, ScreenshotType.ITEM_KIOSK);

  private ScreenshotRepository repository;

  @BeforeEach
  void setUp() {
    repository = new ScreenshotRepository();
  }

  @Test
  void whenUpsertingNewScreenshot_thenSnapshotContainsIt() {
    repository.upsert(SCREENSHOT_A);

    assertEquals(1, repository.getSnapshot().size());
  }

  @Test
  void whenUpsertingNewScreenshot_thenItIsAtHeadOfSnapshot() {
    repository.upsert(SCREENSHOT_A);
    repository.upsert(SCREENSHOT_B);

    assertEquals("id-b", repository.getSnapshot().get(0).id());
  }

  @Test
  void givenExistingId_whenUpserting_thenSnapshotHasNoDuplicates() {
    repository.upsert(SCREENSHOT_A);
    repository.upsert(SCREENSHOT_A);

    assertEquals(1, repository.getSnapshot().size());
  }

  @Test
  void givenExistingId_whenUpsertingWithNewStatus_thenSnapshotReflectsUpdatedStatus() {
    repository.upsert(SCREENSHOT_A);
    var updated = new Screenshot("id-a", null, "Orison", ScreenshotStatus.SUCCESS, null, null,
        ScreenshotType.COMMODITY_KIOSK);

    repository.upsert(updated);

    assertEquals(ScreenshotStatus.SUCCESS, repository.getSnapshot().get(0).status());
  }

  @Test
  void givenExistingIdNotAtHead_whenUpserting_thenItStaysInPlace() {
    repository.upsert(SCREENSHOT_A);
    repository.upsert(SCREENSHOT_B);
    var updatedA = new Screenshot("id-a", null, "Orison", ScreenshotStatus.PROCESSING, null, null,
        ScreenshotType.COMMODITY_KIOSK);

    repository.upsert(updatedA);

    assertEquals("id-b", repository.getSnapshot().get(0).id());
    assertEquals("id-a", repository.getSnapshot().get(1).id());
  }

  @Test
  void given36FullRepository_whenUpsertingAnother_thenSizeRemainsAt36() {
    for (int i = 0; i < 36; i++) {
      repository.upsert(new Screenshot("id-" + i, null, null, ScreenshotStatus.QUEUED, null, null,
          ScreenshotType.COMMODITY_KIOSK));
    }

    repository.upsert(new Screenshot("id-new", null, null, ScreenshotStatus.QUEUED, null, null,
        ScreenshotType.COMMODITY_KIOSK));

    assertEquals(36, repository.getSnapshot().size());
  }

  @Test
  void given36FullRepository_whenUpsertingAnother_thenFirstInsertedIsDropped() {
    repository.upsert(new Screenshot("id-first", null, null, ScreenshotStatus.QUEUED, null, null,
        ScreenshotType.COMMODITY_KIOSK));
    for (int i = 1; i < 36; i++) {
      repository.upsert(new Screenshot("id-" + i, null, null, ScreenshotStatus.QUEUED, null, null,
          ScreenshotType.COMMODITY_KIOSK));
    }

    repository.upsert(new Screenshot("id-new", null, null, ScreenshotStatus.QUEUED, null, null,
        ScreenshotType.COMMODITY_KIOSK));

    assertFalse(repository.getSnapshot().stream().anyMatch(s -> "id-first".equals(s.id())));
  }

  @Test
  void whenGettingSnapshot_thenItIsImmutable() {
    repository.upsert(SCREENSHOT_A);

    assertThrows(UnsupportedOperationException.class,
        () -> repository.getSnapshot().add(SCREENSHOT_B));
  }
}
