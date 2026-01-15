package tools.sctrade.companion.domain.commodity;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.manipulations.EqualizeColor;
import tools.sctrade.companion.domain.image.manipulations.UpscaleTo4k;
import tools.sctrade.companion.domain.image.manipulations.WriteToDisk;
import tools.sctrade.companion.domain.notification.ConsoleNotificationRepository;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.WindowsOcr;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.utils.ProcessRunner;
import tools.sctrade.companion.utils.ResourceUtil;

@ExtendWith(MockitoExtension.class)
class CommoditySubmissionFactoryITest {
  @Mock
  private UserService userService;
  @Mock
  private SettingRepository settings;

  private DiskImageWriter diskImageWriter;
  private ProcessRunner processRunner = new ProcessRunner();

  private LocationRepository locationRepository = new TestLocationRepository();
  private CommodityRepository commodityRepository = new TestCommodityRepository();

  private CommodityLocationReader commodityLocationReader =
      new CommodityLocationReader(locationRepository);
  private CommodityListingFactory commodityListingFactory =
      new CommodityListingFactory(commodityRepository);
  private Ocr ocr;
  private NotificationService notificationService =
      new NotificationService(new ConsoleNotificationRepository());

  private CommoditySubmissionFactory submissionFactory;

  @BeforeEach
  void setUp() {
    setupMocks();

    diskImageWriter = new DiskImageWriter(settings);
    List<ImageManipulation> imageManipulations =
        List.of(new UpscaleTo4k(), new WriteToDisk(diskImageWriter), new EqualizeColor());
    ocr = new WindowsOcr(imageManipulations, diskImageWriter, processRunner,
        new NotificationService(new ConsoleNotificationRepository()));

    submissionFactory = new CommoditySubmissionFactory(userService, notificationService,
        commodityLocationReader, commodityListingFactory, ocr);
  }

  @Test
  void bonjour() throws IOException, URISyntaxException {
    var filename = "canard-view-buy-1";

    String resourcePath = "/kiosks/commodity/images/" + filename + ".jpg";
    var image = ResourceUtil.getBufferedImage(resourcePath);

    var submission = submissionFactory.build(image);
  }

  private void setupMocks() {
    when(settings.get(Setting.OUTPUT_TRANSIENT_IMAGES)).thenReturn(true);
    when(settings.get(Setting.OUTPUT_SCREENSHOTS)).thenReturn(true);
    when(settings.get(Setting.MY_IMAGES_PATH))
        .thenReturn(Paths.get(".", "test-images").normalize().toAbsolutePath());
  }
}
