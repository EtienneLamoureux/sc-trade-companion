package tools.sctrade.companion.spring;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.CompanionVersionRepository;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.gui.CompanionGui;
import tools.sctrade.companion.utils.LocalizationUtil;

@ExtendWith(MockitoExtension.class)
class CompanionVersionCheckerTest {
  private static final String CURRENT_VERSION = "1.0.0";
  private static final String LATEST_VERSION = "1.1.0";

  @Mock
  private CompanionVersionRepository mockRepository;
  @Mock
  private CompanionGui mockGui;
  @Mock
  private NotificationService mockNotificationService;

  private CompanionVersionChecker checker;

  @BeforeEach
  void setUp() {
    checker = new CompanionVersionChecker(mockRepository, mockGui, mockNotificationService,
        CURRENT_VERSION);
  }

  @Test
  void givenVersionsDifferWhenCheckingThenShowsPopup()
      throws InterruptedException, InvocationTargetException {
    when(mockRepository.fetchLatestVersion()).thenReturn(LATEST_VERSION);

    checker.check();
    flushEdt();

    verify(mockGui).showUpdateAvailablePopup(CURRENT_VERSION, LATEST_VERSION);
  }

  @Test
  void givenVersionsMatchWhenCheckingThenDoesNotShowPopup()
      throws InterruptedException, InvocationTargetException {
    when(mockRepository.fetchLatestVersion()).thenReturn(CURRENT_VERSION);

    checker.check();
    flushEdt();

    verify(mockGui, never()).showUpdateAvailablePopup(anyString(), anyString());
  }

  @Test
  void givenPopupAlreadyShownWhenCheckingAgainThenDoesNotShowPopupAgain()
      throws InterruptedException, InvocationTargetException {
    when(mockRepository.fetchLatestVersion()).thenReturn(LATEST_VERSION);
    checker.check();
    flushEdt();

    checker.check();
    flushEdt();

    verify(mockGui).showUpdateAvailablePopup(eq(CURRENT_VERSION), eq(LATEST_VERSION));
  }

  @Test
  void givenRepositoryFailsWhenCheckingThenWarns()
      throws InterruptedException, InvocationTargetException {
    when(mockRepository.fetchLatestVersion()).thenThrow(new RuntimeException("network error"));

    checker.check();
    flushEdt();

    verify(mockNotificationService).warn(LocalizationUtil.get("warningUnableToCheckLatestVersion"));
  }

  @Test
  void givenRepositoryFailsWhenCheckingThenDoesNotShowPopup()
      throws InterruptedException, InvocationTargetException {
    when(mockRepository.fetchLatestVersion()).thenThrow(new RuntimeException("network error"));

    checker.check();
    flushEdt();

    verify(mockGui, never()).showUpdateAvailablePopup(anyString(), anyString());
  }

  private static void flushEdt() throws InterruptedException, InvocationTargetException {
    EventQueue.invokeAndWait(() -> {
    });
  }
}
