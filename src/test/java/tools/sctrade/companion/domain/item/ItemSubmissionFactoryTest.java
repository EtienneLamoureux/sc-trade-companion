package tools.sctrade.companion.domain.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
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
class ItemSubmissionFactoryTest {

  @Mock
  private UserService userService;
  @Mock
  private Ocr ocr;
  @Mock
  private ItemLocationReader itemLocationReader;
  @Mock
  private ItemShopReader itemShopReader;
  @Mock
  private ItemListingFactory itemListingFactory;

  private NotificationService notificationService;
  private ItemSubmissionFactory factory;
  private BufferedImage image;

  private static final User USER = new User("id", "label");

  @BeforeEach
  void setUp() {
    notificationService = new NotificationService(new ConsoleNotificationRepository());
    image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

    factory = new ItemSubmissionFactory(userService, notificationService, itemListingFactory,
        itemLocationReader, itemShopReader, ocr);
  }

  @Test
  void whenBuildSucceeds_thenSubmissionContainsListings() {
    when(ocr.read(any())).thenReturn(new OcrResult(List.of()));
    when(itemLocationReader.read(any(BufferedImage.class), any(OcrResult.class)))
        .thenReturn(Optional.of("Area18"));
    when(itemShopReader.read(any(BufferedImage.class), any(OcrResult.class)))
        .thenReturn(Optional.of("Casaba"));
    when(itemListingFactory.build(any(OcrResult.class), any(), any()))
        .thenReturn(List.of(new ItemListing("item", 1.0, "Area18", "Casaba")));
    when(userService.get()).thenReturn(USER);

    var submission = factory.build(image);
    assertEquals(1, submission.getListings().size());
  }

  @Test
  void whenLocationAndShopExistButNoListings_thenBuildThrowsNoListingsException() {
    when(ocr.read(any())).thenReturn(new OcrResult(List.of()));
    when(itemLocationReader.read(any(BufferedImage.class), any(OcrResult.class)))
        .thenReturn(Optional.of("Area18"));
    when(itemShopReader.read(any(BufferedImage.class), any(OcrResult.class)))
        .thenReturn(Optional.of("Casaba"));
    when(itemListingFactory.build(any(OcrResult.class), any(), any())).thenReturn(List.of());
    org.junit.jupiter.api.Assertions.assertThrows(NoListingsException.class,
        () -> factory.build(image));
  }
}
