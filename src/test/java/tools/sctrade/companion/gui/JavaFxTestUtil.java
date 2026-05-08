package tools.sctrade.companion.gui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javafx.application.Platform;

public final class JavaFxTestUtil {
  private static final AtomicBoolean toolkitStarted = new AtomicBoolean(false);

  private JavaFxTestUtil() {}

  public static void startToolkit() {
    if (toolkitStarted.compareAndSet(false, true)) {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.startup(latch::countDown);
      await(latch);
    }
  }

  public static void runOnFxThreadAndWait(Runnable runnable) {
    startToolkit();

    if (Platform.isFxApplicationThread()) {
      runnable.run();
      return;
    }

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> failure = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        runnable.run();
      } catch (Throwable throwable) {
        failure.set(throwable);
      } finally {
        latch.countDown();
      }
    });
    await(latch);

    if (failure.get() != null) {
      throw new AssertionError(failure.get());
    }
  }

  public static <T> T supplyOnFxThreadAndWait(Supplier<T> supplier) {
    AtomicReference<T> value = new AtomicReference<>();
    runOnFxThreadAndWait(() -> value.set(supplier.get()));
    return value.get();
  }

  private static void await(CountDownLatch latch) {
    try {
      if (!latch.await(10, TimeUnit.SECONDS)) {
        throw new AssertionError("Timed out waiting for JavaFX");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Interrupted while waiting for JavaFX", e);
    }
  }
}
