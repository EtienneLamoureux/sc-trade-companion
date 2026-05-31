package tools.sctrade.companion.gui.screenshot;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.utils.Processor;

@ExtendWith(MockitoExtension.class)
class ScreenshotSubmissionFacadeTest {

  private static final ScreenshotType TYPE = ScreenshotType.COMMODITY_KIOSK;

  @Mock
  private Processor<BufferedImage> asyncProcessor;
  @Mock
  private ScreenshotRepository screenshotRepository;
  @Mock
  private ScreenshotFactory screenshotFactory;

  private BufferedImage image;

  @BeforeEach
  void setUp() {
    image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
  }

  @Test
  void givenScreenCapture_whenProcess_thenUpsertsQueuedScreenshotBeforeAsyncDelegation() {
    String id = StatusTrackingSubmissionFactory.computeId(TYPE, image);
    Screenshot queued = new Screenshot(id, image, null, ScreenshotStatus.QUEUED, null, null, TYPE);
    when(screenshotFactory.buildQueued(anyString(), same(image), eq(TYPE))).thenReturn(queued);
    ScreenshotSubmissionFacade facade = new ScreenshotSubmissionFacade(asyncProcessor,
        screenshotRepository, screenshotFactory, TYPE);

    facade.process(image);

    InOrder inOrder = inOrder(screenshotFactory, screenshotRepository, asyncProcessor);
    inOrder.verify(screenshotFactory).buildQueued(id, image, TYPE);
    inOrder.verify(screenshotRepository).upsert(queued);
    inOrder.verify(asyncProcessor).process(image);
  }

  @Test
  void givenScreenCapture_whenProcess_thenDelegatesOriginalImageToAsyncProcessor() {
    when(screenshotFactory.buildQueued(anyString(), same(image), eq(TYPE)))
        .thenReturn(new Screenshot("id", image, null, ScreenshotStatus.QUEUED, null, null, TYPE));
    ScreenshotSubmissionFacade facade = new ScreenshotSubmissionFacade(asyncProcessor,
        screenshotRepository, screenshotFactory, TYPE);

    facade.process(image);

    verify(asyncProcessor).process(same(image));
  }
}
