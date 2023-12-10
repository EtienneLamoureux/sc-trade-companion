package tools.sctrade.companion.spring;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import tools.sctrade.companion.domain.commodity.CommodityService;
import tools.sctrade.companion.domain.commodity.CommoditySubmissionFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.image.manipulations.UpscaleTo4k;
import tools.sctrade.companion.domain.notification.NotificationRepository;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.user.Setting;
import tools.sctrade.companion.domain.user.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.input.KeyListener;
import tools.sctrade.companion.input.ScreenPrinter;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.output.commodity.CommodityCsvWriter;
import tools.sctrade.companion.output.commodity.ScTradeToolsClient;
import tools.sctrade.companion.swing.CompanionGui;
import tools.sctrade.companion.swing.LogsTab;
import tools.sctrade.companion.utils.SoundUtil;

@Configuration
@EnableCaching
public class AppConfig {
  @Autowired(required = false)
  private BuildProperties buildProperties;

  @Value("${output.screenshots:true}")
  private String outputScreenshots;
  @Value("${output.intermediary-images:false}")
  private String outputIntermediaryImages;

  @Bean("SettingRepository")
  public SettingRepository buildSettingRepository() {
    var settingRepository = new SettingRepository();
    settingRepository.set(Setting.MY_IMAGES_PATH,
        Paths.get(".", "my-images").normalize().toAbsolutePath());
    settingRepository.set(Setting.MY_DATA_PATH,
        Paths.get(".", "my-data").normalize().toAbsolutePath());
    settingRepository.set(Setting.OUTPUT_SCREENSHOTS, outputScreenshots);
    settingRepository.set(Setting.OUTPUT_TRANSIENT_IMAGES, outputIntermediaryImages);
    settingRepository.set(Setting.SC_TRADE_TOOLS_ROOT_URL, "https://sc-trade.tools");

    return settingRepository;
  }

  @Bean("LogsTab")
  public LogsTab buildLogsTab() {
    return new LogsTab();
  }

  @Bean("CompanionGui")
  public CompanionGui buildCompanionGui() {
    String version = buildProperties == null ? "TEST" : buildProperties.getVersion();

    return new CompanionGui(version);
  }

  @Bean("NotificationService")
  public NotificationService buildNotificationService(NotificationRepository repository) {
    return new NotificationService(repository);
  }

  @Bean("UserService")
  public UserService buildUserService() {
    return new UserService();
  }

  @Bean("CommodityCsvWriter")
  public CommodityCsvWriter buildCommodityCsvLogger(SettingRepository settingRepository) {
    return new CommodityCsvWriter(settingRepository);
  }

  @Bean("ScTradeToolsClient")
  public ScTradeToolsClient buildScTradeToolsClient(WebClient.Builder webClientBuilder,
      SettingRepository settings) {
    return new ScTradeToolsClient(webClientBuilder, settings);
  }

  @Bean("DiskImageWriter")
  public DiskImageWriter buildDiskImageWriter(SettingRepository settingRepository) {
    return new DiskImageWriter(settingRepository);
  }

  @Bean("CommoditySubmissionFactory")
  public CommoditySubmissionFactory buildCommoditySubmissionFactory(UserService userService,
      @Qualifier("ScTradeToolsClient") ScTradeToolsClient scTradeToolsClient,
      ImageWriter imageWriter) {
    return new CommoditySubmissionFactory(userService, scTradeToolsClient, scTradeToolsClient,
        imageWriter);
  }

  @Bean("CommodityService")
  public CommodityService buildCommodityService(
      CommoditySubmissionFactory commoditySubmissionFactory,
      @Qualifier("CommodityCsvWriter") CommodityCsvWriter commodityCsvLogger,
      @Qualifier("ScTradeToolsClient") ScTradeToolsClient scTradeToolsClient) {
    return new CommodityService(commoditySubmissionFactory,
        Arrays.asList(commodityCsvLogger, scTradeToolsClient));
  }

  @Bean
  public SoundUtil buildSoundUtil() {
    return new SoundUtil();
  }

  @Bean("ScreenPrinter")
  public ScreenPrinter buildScreenPrinter(
      @Qualifier("CommodityService") CommodityService commodityService, ImageWriter imageWriter,
      SoundUtil soundPlayer) {
    List<ImageManipulation> postprocessingManipulations = new ArrayList<>();
    postprocessingManipulations.add(new UpscaleTo4k());

    return new ScreenPrinter(Arrays.asList(commodityService), postprocessingManipulations,
        imageWriter, soundPlayer);
  }

  @Bean("KeyListener")
  public KeyListener buildNativeKeyListener(@Qualifier("ScreenPrinter") Runnable screenPrinter) {
    return new KeyListener(Arrays.asList(screenPrinter));
  }

}
