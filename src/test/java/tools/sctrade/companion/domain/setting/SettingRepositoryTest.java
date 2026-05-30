package tools.sctrade.companion.domain.setting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class SettingRepositoryTest {
  @Test
  void givenMissingKeybindWhenGettingDefaultThenReturnsDefaultWithoutWarning() {
    SettingRepository repository = new SettingRepository();
    clearSettings(repository);
    Logger logger = (Logger) LoggerFactory.getLogger(SettingRepository.class);
    Level originalLevel = logger.getLevel();
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.setContext(logger.getLoggerContext());
    appender.start();
    logger.setLevel(Level.WARN);
    logger.addAppender(appender);

    try {
      int value = repository.get(Setting.PRINTSCREEN_ITEM_KEYBIND, 777);

      assertEquals(777, value);
      assertFalse(appender.list.stream().anyMatch(event -> event.getFormattedMessage()
          .contains("Could not retreive the value of the PRINTSCREEN_ITEM_KEYBIND setting")));
    } finally {
      logger.detachAppender(appender);
      logger.setLevel(originalLevel);
    }
  }

  @Test
  void givenMalformedKeybindWhenGettingDefaultThenReturnsDefaultAndLogsWarning() {
    SettingRepository repository = new SettingRepository();
    clearSettings(repository);
    putRawSetting(repository, Setting.PRINTSCREEN_ITEM_KEYBIND, "invalid-key");
    Logger logger = (Logger) LoggerFactory.getLogger(SettingRepository.class);
    Level originalLevel = logger.getLevel();
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.setContext(logger.getLoggerContext());
    appender.start();
    logger.setLevel(Level.WARN);
    logger.addAppender(appender);

    try {
      int value = repository.get(Setting.PRINTSCREEN_ITEM_KEYBIND, 777);

      assertEquals(777, value);
      assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage()
          .contains("Could not retreive the value of the PRINTSCREEN_ITEM_KEYBIND setting")));
    } finally {
      logger.detachAppender(appender);
      logger.setLevel(originalLevel);
    }
  }

  @Test
  void givenConcurrentReadWriteWhenGettingDefaultsThenNoReadFails() throws Exception {
    SettingRepository repository = new SettingRepository();
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    AtomicReference<Throwable> failure = new AtomicReference<>();

    Thread writer = new Thread(() -> {
      try {
        ready.countDown();
        if (!start.await(2, TimeUnit.SECONDS)) {
          throw new AssertionError("Writer start timed out");
        }
        for (int i = 0; i < 5000; i++) {
          repository.set(Setting.OUTPUT_SCREENSHOTS, i % 2 == 0);
        }
      } catch (Throwable t) {
        failure.compareAndSet(null, t);
      }
    });
    Thread reader = new Thread(() -> {
      try {
        ready.countDown();
        if (!start.await(2, TimeUnit.SECONDS)) {
          throw new AssertionError("Reader start timed out");
        }
        for (int i = 0; i < 5000; i++) {
          repository.get(Setting.OUTPUT_SCREENSHOTS, Boolean.FALSE);
        }
      } catch (Throwable t) {
        failure.compareAndSet(null, t);
      }
    });

    writer.start();
    reader.start();
    assertTrue(ready.await(2, TimeUnit.SECONDS));
    start.countDown();
    writer.join(3000);
    reader.join(3000);

    Throwable error = failure.get();
    if (error != null) {
      throw new AssertionError("Concurrent access failed", error);
    }
  }

  @SuppressWarnings("unchecked")
  private static void clearSettings(SettingRepository repository) {
    try {
      Field settingsField = SettingRepository.class.getDeclaredField("settings");
      settingsField.setAccessible(true);
      ((Map<Setting, String>) settingsField.get(repository)).clear();
    } catch (Exception e) {
      throw new AssertionError("Unable to clear settings", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static void putRawSetting(SettingRepository repository, Setting setting, String value) {
    try {
      Field settingsField = SettingRepository.class.getDeclaredField("settings");
      settingsField.setAccessible(true);
      ((Map<Setting, String>) settingsField.get(repository)).put(setting, value);
    } catch (Exception e) {
      throw new AssertionError("Unable to put raw setting", e);
    }
  }
}
