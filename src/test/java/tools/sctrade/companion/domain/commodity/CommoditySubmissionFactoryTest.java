package tools.sctrade.companion.domain.commodity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.notification.ConsoleNotificationRepository;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.user.User;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.exceptions.NoListingsException;

@ExtendWith(MockitoExtension.class)
class CommoditySubmissionFactoryTest {

  @Mock
  private UserService userService;
  @Mock
  private Ocr ocr;
  @Mock
  private CommodityLocationReader commodityLocationReader;
  @Mock
  private CommodityListingFactory commodityListingFactory;

  private NotificationService notificationService;
  private CommoditySubmissionFactory factory;
  private BufferedImage image;

  private static final User USER = new User("id", "label");
  private static final CommodityListing TEST_LISTING = new CommodityListing("Area18",
      TransactionType.SELLS, "Aluminum", List.of(1), "batch", Instant.now());

  @BeforeEach
  void setUp() {
    notificationService = new NotificationService(new ConsoleNotificationRepository());
    image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

    factory = new CommoditySubmissionFactory(userService, notificationService,
        commodityLocationReader, commodityListingFactory, ocr);
  }

  @Test
  void whenBuildSucceeds_thenSubmissionContainsListings() {
    when(ocr.read(any())).thenReturn(new OcrResult(List.of()));
    when(commodityLocationReader.read(any(BufferedImage.class), any(OcrResult.class)))
        .thenReturn(Optional.empty());
    when(commodityListingFactory.build(any(OcrResult.class), any()))
        .thenReturn(List.of(TEST_LISTING));
    when(userService.get()).thenReturn(USER);

    var submission = factory.build(image);

    assertEquals(1, submission.getListings().size());
  }

  @Test
  void whenNoListingsRead_thenBuildThrowsNoListingsException() {
    when(ocr.read(any())).thenReturn(new OcrResult(List.of()));
    when(commodityLocationReader.read(any(BufferedImage.class), any(OcrResult.class)))
        .thenReturn(Optional.empty());
    when(commodityListingFactory.build(any(OcrResult.class), any())).thenReturn(List.of());
    org.junit.jupiter.api.Assertions.assertThrows(NoListingsException.class,
        () -> factory.build(image));
  }
}
