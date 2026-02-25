package tools.sctrade.companion.domain.commodity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.notification.ConsoleNotificationRepository;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OneOcr;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.utils.ProcessRunner;
import tools.sctrade.companion.utils.ResourceUtil;

@Disabled("Need to be updated for the new OneOcrWrapper output format")
@ExtendWith(MockitoExtension.class)
class CommoditySubmissionFactoryComponentTest {
  @Mock
  private UserService userService;
  @Mock
  private DiskImageWriter diskImageWriter;
  @Mock
  private ProcessRunner processRunner;

  private LocationRepository locationRepository = new TestLocationRepository();
  private CommodityRepository commodityRepository = new TestCommodityRepository();

  private CommodityLocationReader commodityLocationReader;
  private CommodityListingFactory commodityListingFactory;
  private Ocr ocr;
  private NotificationService notificationService;

  private CommoditySubmissionFactory submissionFactory;

  @BeforeEach
  void setUp() {
    ocr = new OneOcr(List.of(), diskImageWriter);
    commodityLocationReader = new CommodityLocationReader(locationRepository);
    commodityListingFactory = new CommodityListingFactory(commodityRepository);
    notificationService = new NotificationService(new ConsoleNotificationRepository());

    submissionFactory = new CommoditySubmissionFactory(userService, notificationService,
        commodityLocationReader, commodityListingFactory, ocr);
  }

  @Test
  void bonjour() throws IOException, URISyntaxException {
    var filename = "arc-l2-sell-1";

    String resourcePath = "/kiosks/commodity/images/" + filename + ".jpg";
    when(diskImageWriter.write(any(), any()))
        .thenReturn(Optional.of(Path.of(this.getClass().getResource(resourcePath).toURI())));
    when(processRunner.runNoFail(any()))
        .thenReturn(ResourceUtil.getTextLines("/kiosks/commodity/actuals/" + filename + ".json"));

    var image = ResourceUtil.getBufferedImage(resourcePath);

    var submission = submissionFactory.build(image);
  }
}
