package tools.sctrade.companion.spring;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.utils.Consumer;

class AppConfigConsumerRestartTest {
  @Test
  void givenConsumerFailureWhenRestartingThenRestartLogIncludesTriggeringException() {
    BlockingQueue<BufferedImage> queue = new ArrayBlockingQueue<>(5);
    queue.add(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    NotificationService notificationService = mock(NotificationService.class);
    TestRestartingConsumer consumer = new TestRestartingConsumer(queue, notificationService);
    AppConfig appConfig = new AppConfig();
    Logger logger = (Logger) LoggerFactory.getLogger(AppConfig.class);
    Level originalLevel = logger.getLevel();
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.setContext(logger.getLoggerContext());
    appender.start();
    logger.setLevel(Level.ERROR);
    logger.addAppender(appender);
    try {
      invokeStartConsumerWithAutoRestart(appConfig, consumer, "Commodity Kiosk",
          notificationService);
      waitForCondition(() -> consumer.getInvocationCount() >= 1, 10000, 50);

      waitForCondition(() -> appender.list.stream().anyMatch(
          event -> event.getFormattedMessage().contains("Restarting consumer: Commodity Kiosk")),
          10000, 50);

      ILoggingEvent restartEvent = appender.list.stream()
          .filter(
              event -> event.getFormattedMessage().contains("Restarting consumer: Commodity Kiosk"))
          .findFirst().orElseThrow();
      assertNotNull(restartEvent.getThrowableProxy());
    } finally {
      logger.detachAppender(appender);
      logger.setLevel(originalLevel);
    }
  }

  @Test
  void givenRestartNotificationFailureWhenConsumerRestartsThenSupervisorKeepsRunning() {
    BlockingQueue<BufferedImage> queue = new ArrayBlockingQueue<>(5);
    queue.add(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
    NotificationService notificationService = mock(NotificationService.class);
    doThrow(new RuntimeException("notification-failed")).when(notificationService)
        .info("Screenshot consumer (Commodity Kiosk) restarting...");
    TestRestartingConsumer consumer = new TestRestartingConsumer(queue, notificationService);
    AppConfig appConfig = new AppConfig();

    invokeStartConsumerWithAutoRestart(appConfig, consumer, "Commodity Kiosk", notificationService);
    queue.add(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

    waitForCondition(() -> consumer.getSuccessfulConsumeCount() >= 1, 10000, 50);
    assertTrue(consumer.getSuccessfulConsumeCount() >= 1);
  }

  private static void invokeStartConsumerWithAutoRestart(AppConfig appConfig,
      Consumer<BufferedImage> consumer, String consumerName,
      NotificationService notificationService) {
    try {
      Method method = AppConfig.class.getDeclaredMethod("startConsumerWithAutoRestart",
          Consumer.class, String.class, NotificationService.class);
      method.setAccessible(true);
      method.invoke(appConfig, consumer, consumerName, notificationService);
    } catch (Exception e) {
      throw new AssertionError("Failed to invoke startConsumerWithAutoRestart", e);
    }
  }

  private static void waitForCondition(BooleanSupplier condition, long timeoutMs, long pollMs) {
    long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
    while (System.nanoTime() < deadline) {
      if (condition.getAsBoolean()) {
        return;
      }

      try {
        Thread.sleep(pollMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new AssertionError("Interrupted while waiting for condition", e);
      }
    }

    throw new AssertionError("Condition was not satisfied within timeout");
  }

  private static class TestRestartingConsumer extends Consumer<BufferedImage> {
    private final AtomicInteger invocationCount = new AtomicInteger();
    private final AtomicInteger successfulConsumeCount = new AtomicInteger();

    TestRestartingConsumer(BlockingQueue<BufferedImage> queue,
        NotificationService notificationService) {
      super(queue, notificationService);
    }

    @Override
    protected void consume(BufferedImage item) throws Exception {
      int current = invocationCount.incrementAndGet();
      if (current == 1) {
        throw new RuntimeException("first-consume-fails");
      }

      successfulConsumeCount.incrementAndGet();
    }

    int getSuccessfulConsumeCount() {
      return successfulConsumeCount.get();
    }

    int getInvocationCount() {
      return invocationCount.get();
    }
  }
}
