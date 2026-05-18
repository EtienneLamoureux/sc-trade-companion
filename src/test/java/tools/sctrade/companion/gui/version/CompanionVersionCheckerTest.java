package tools.sctrade.companion.gui.version;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.gui.JavaFxTestUtil;
import tools.sctrade.companion.utils.LocalizationUtil;

@ExtendWith(MockitoExtension.class)
class CompanionVersionCheckerTest {
  private static final String CURRENT_VERSION = "1.0.0";
  private static final String LATEST_VERSION = "1.1.0";

  @Mock
  private CompanionVersionRepository mockRepository;
  @Mock
  private UpdateAvailablePopup mockPopup;
  @Mock
  private NotificationService mockNotificationService;

  private CompanionVersionChecker checker;

  @BeforeEach
  void setUp() {
    JavaFxTestUtil.startToolkit();
    checker = new CompanionVersionChecker(mockRepository, mockPopup, mockNotificationService,
        CURRENT_VERSION);
  }

  @Test
  void givenVersionsDifferWhenCheckingThenShowsPopup() {
    when(mockRepository.fetchLatestVersion()).thenReturn(LATEST_VERSION);

    checker.check();
    flushJavaFx();

    verify(mockPopup).showUpdateAvailablePopup(CURRENT_VERSION, LATEST_VERSION);
  }

  @Test
  void givenVersionsMatchWhenCheckingThenDoesNotShowPopup() {
    when(mockRepository.fetchLatestVersion()).thenReturn(CURRENT_VERSION);

    checker.check();
    flushJavaFx();

    verify(mockPopup, never()).showUpdateAvailablePopup(anyString(), anyString());
  }

  @Test
  void givenPopupAlreadyShownWhenCheckingAgainThenDoesNotShowPopupAgain() {
    when(mockRepository.fetchLatestVersion()).thenReturn(LATEST_VERSION);
    checker.check();
    flushJavaFx();

    checker.check();
    flushJavaFx();

    verify(mockPopup, times(1)).showUpdateAvailablePopup(eq(CURRENT_VERSION), eq(LATEST_VERSION));
  }

  @Test
  void givenRepositoryFailsWhenCheckingThenWarns() {
    when(mockRepository.fetchLatestVersion()).thenThrow(new RuntimeException("network error"));

    checker.check();
    flushJavaFx();

    verify(mockNotificationService).warn(LocalizationUtil.get("warningUnableToCheckLatestVersion"));
  }

  @Test
  void givenRepositoryFailsWhenCheckingThenDoesNotShowPopup() {
    when(mockRepository.fetchLatestVersion()).thenThrow(new RuntimeException("network error"));

    checker.check();
    flushJavaFx();

    verify(mockPopup, never()).showUpdateAvailablePopup(anyString(), anyString());
  }

  @Test
  void givenBlankVersionResponseWhenCheckingThenWarns() {
    when(mockRepository.fetchLatestVersion())
        .thenThrow(new IllegalStateException("Received null or blank latest-version response"));

    checker.check();
    flushJavaFx();

    verify(mockNotificationService).warn(LocalizationUtil.get("warningUnableToCheckLatestVersion"));
  }

  @Test
  void givenBlankVersionResponseWhenCheckingThenDoesNotShowPopup() {
    when(mockRepository.fetchLatestVersion())
        .thenThrow(new IllegalStateException("Received null or blank latest-version response"));

    checker.check();
    flushJavaFx();

    verify(mockPopup, never()).showUpdateAvailablePopup(anyString(), anyString());
  }

  private static void flushJavaFx() {
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
    });
  }
}
