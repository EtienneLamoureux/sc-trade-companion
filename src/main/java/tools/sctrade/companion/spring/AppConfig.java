package tools.sctrade.companion.spring;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.input.TailerListener;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import tools.sctrade.companion.domain.LocationRepository;
import tools.sctrade.companion.domain.commodity.CommodityListingFactory;
import tools.sctrade.companion.domain.commodity.CommodityLocationReader;
import tools.sctrade.companion.domain.commodity.CommodityRepository;
import tools.sctrade.companion.domain.commodity.CommodityService;
import tools.sctrade.companion.domain.commodity.CommoditySubmissionFactory;
import tools.sctrade.companion.domain.gamelog.GameLogPathSubject;
import tools.sctrade.companion.domain.gamelog.lineprocessors.FallbackLogLineProcessor;
import tools.sctrade.companion.domain.gamelog.lineprocessors.LoadShopInventoryDataLogLineProcessor;
import tools.sctrade.companion.domain.gamelog.lineprocessors.OldLogLineProcessor;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.image.manipulations.UpscaleTo4k;
import tools.sctrade.companion.domain.item.ItemListingFactory;
import tools.sctrade.companion.domain.item.ItemLocationReader;
import tools.sctrade.companion.domain.item.ItemService;
import tools.sctrade.companion.domain.item.ItemShopReader;
import tools.sctrade.companion.domain.item.ItemShopRepository;
import tools.sctrade.companion.domain.item.ItemSubmissionFactory;
import tools.sctrade.companion.domain.notification.NotificationRepository;
import tools.sctrade.companion.domain.notification.NotificationService;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OneOcr;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.UserIdGenerator;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.domain.user.idgenerators.BestEffortUserIdGenerator;
import tools.sctrade.companion.domain.user.idgenerators.HardwareUserIdGenerator;
import tools.sctrade.companion.domain.user.idgenerators.RandomUserIdGenerator;
import tools.sctrade.companion.domain.user.idgenerators.WindowsUserIdGenerator;
import tools.sctrade.companion.gui.CompanionGui;
import tools.sctrade.companion.gui.LogsTab;
import tools.sctrade.companion.gui.version.CompanionVersionChecker;
import tools.sctrade.companion.gui.version.CompanionVersionRepository;
import tools.sctrade.companion.gui.version.UpdateAvailablePopup;
import tools.sctrade.companion.input.KeyListener;
import tools.sctrade.companion.input.LineListener;
import tools.sctrade.companion.input.ScreenPrinter;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.output.ScTradeToolsClient;
import tools.sctrade.companion.output.ScTradeToolsCompanionVersionRepository;
import tools.sctrade.companion.output.ScTradeToolsLocationRepository;
import tools.sctrade.companion.output.commodity.CommodityCsvWriter;
import tools.sctrade.companion.output.commodity.ScTradeToolsCommodityPublisher;
import tools.sctrade.companion.output.item.ItemCsvWriter;
import tools.sctrade.companion.output.item.ScTradeToolsItemPublisher;
import tools.sctrade.companion.output.item.ScTradeToolsItemRepository;
import tools.sctrade.companion.output.item.ScTradeToolsItemShopRepository;
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
  @Value("${tools.sctrade.root-url:https://sc-trade.tools}")
  private String scTradeToolsRootUrl;

  @Bean("SettingRepository")
  public SettingRepository buildSettingRepository() {
    var settingRepository = new SettingRepository();
    settingRepository.set(Setting.MY_IMAGES_PATH,
        Paths.get(".", "my-images").normalize().toAbsolutePath());
    settingRepository.set(Setting.MY_DATA_PATH,
        Paths.get(".", "my-data").normalize().toAbsolutePath());
    settingRepository.set(Setting.OUTPUT_SCREENSHOTS, outputScreenshots);
    settingRepository.set(Setting.OUTPUT_TRANSIENT_IMAGES, outputIntermediaryImages);
    settingRepository.set(Setting.SC_TRADE_TOOLS_ROOT_URL, scTradeToolsRootUrl);

    return settingRepository;
  }

  @Bean("LogsTab")
  public LogsTab buildLogsTab() {
    return new LogsTab();
  }

  @Bean("UserIdGenerator")
  public UserIdGenerator buildUserIdGenerator() {
    return new BestEffortUserIdGenerator(new WindowsUserIdGenerator(),
        new HardwareUserIdGenerator(), new RandomUserIdGenerator());
  }

  @Bean("UserService")
  public UserService buildUserService(SettingRepository settings, UserIdGenerator userIdGenerator) {
    return new UserService(settings, userIdGenerator);
  }


  @Bean("GameLogPathSubject")
  public GameLogPathSubject buildGameLogSubject(SettingRepository settings) {
    return new GameLogPathSubject(settings);
  }

  @Bean("TailerListener")
  public TailerListener buildTailerListener(CommodityListingFactory commodityListingFactory,
      CommodityService commodityService, NotificationService notificationService) {
    var oldLogLineProcessor = new OldLogLineProcessor();
    var loadShopInventoryDataLogLineProcessor = new LoadShopInventoryDataLogLineProcessor(
        commodityListingFactory, commodityService, notificationService);
    var fallbackLogLineProcessor = new FallbackLogLineProcessor();

    oldLogLineProcessor.setNext(loadShopInventoryDataLogLineProcessor);
    loadShopInventoryDataLogLineProcessor.setNext(fallbackLogLineProcessor);

    return new LineListener(oldLogLineProcessor);
  }

  // @Bean("FileTailer")
  // public FileTailer buildFileTailer(Subject<Path> subject, TailerListener listener,
  // NotificationService notificationService) {
  // return new FileTailer(subject, listener, notificationService);
  // }

  @Bean("CompanionGui")
  public CompanionGui buildCompanionGui(UserService userService, GameLogPathSubject gameLogService,
      SettingRepository settings) {
    return new CompanionGui(userService, gameLogService, settings, getVersion());
  }

  @Bean("NotificationService")
  public NotificationService buildNotificationService(NotificationRepository repository) {
    return new NotificationService(repository);
  }

  @Bean("CommodityCsvWriter")
  public CommodityCsvWriter buildCommodityCsvLogger(SettingRepository settingRepository,
      NotificationService notificationService) {
    return new CommodityCsvWriter(settingRepository, notificationService);
  }

  @Bean("ItemCsvWriter")
  public ItemCsvWriter buildItemCsvWriter(SettingRepository settingRepository,
      NotificationService notificationService) {
    return new ItemCsvWriter(settingRepository, notificationService);
  }

  @Bean
  public ScTradeToolsClient buildScTradeToolsClient(WebClient.Builder webClientBuilder,
      SettingRepository settings) {
    return new ScTradeToolsClient(webClientBuilder, settings, getVersion());
  }

  @Bean
  public CompanionVersionRepository buildCompanionVersionRepository(ScTradeToolsClient client) {
    return new ScTradeToolsCompanionVersionRepository(client);
  }

  @Bean
  public CompanionVersionChecker buildCompanionVersionChecker(CompanionVersionRepository repository,
      UpdateAvailablePopup popup, NotificationService notificationService) {
    return new CompanionVersionChecker(repository, popup, notificationService, getVersion());
  }

  @Bean
  public ScTradeToolsLocationRepository buildScTradeToolsLocationRepository(
      ScTradeToolsClient client) {
    return new ScTradeToolsLocationRepository(client);
  }

  @Bean
  public ScTradeToolsItemRepository buildScTradeToolsItemRepository(ScTradeToolsClient client) {
    return new ScTradeToolsItemRepository(client);
  }

  @Bean
  public ScTradeToolsItemShopRepository buildScTradeToolsItemShopRepository(
      ScTradeToolsClient client) {
    return new ScTradeToolsItemShopRepository(client);
  }

  @Bean
  public ScTradeToolsCommodityPublisher buildScTradeToolsCommodityPublisher(
      ScTradeToolsClient client, NotificationService notificationService) {
    return new ScTradeToolsCommodityPublisher(client, notificationService);
  }

  @Bean
  public ScTradeToolsItemPublisher buildScTradeToolsItemPublisher(ScTradeToolsClient client,
      NotificationService notificationService) {
    return new ScTradeToolsItemPublisher(client, notificationService);
  }

  @Bean("DiskImageWriter")
  public DiskImageWriter buildDiskImageWriter(SettingRepository settingRepository) {
    return new DiskImageWriter(settingRepository);

  }

  @Bean("CommodityLocationReader")
  public CommodityLocationReader buildCommodityLocationReader(
      LocationRepository locationRepository) {
    return new CommodityLocationReader(locationRepository);
  }

  @Bean("CommodityListingFactory")
  public CommodityListingFactory buildCommodityListingFactory(
      CommodityRepository commodityRepository) {
    return new CommodityListingFactory(commodityRepository);
  }

  @Bean("CommoditySubmissionFactory")
  public CommoditySubmissionFactory buildCommoditySubmissionFactory(UserService userService,
      NotificationService notificationService, CommodityLocationReader commodityLocationReader,
      CommodityListingFactory commodityListingFactory, DiskImageWriter diskImageWriter) {
    Ocr ocr = new OneOcr(List.of(), diskImageWriter);

    return new CommoditySubmissionFactory(userService, notificationService, commodityLocationReader,
        commodityListingFactory, ocr);
  }

  @Bean("ItemListingFactory")
  public ItemListingFactory buildItemListingFactory(
      ScTradeToolsItemRepository scTradeToolsItemRepository) {
    return new ItemListingFactory(scTradeToolsItemRepository);
  }

  @Bean("ItemLocationReader")
  public ItemLocationReader buildItemLocationReader(LocationRepository locationRepository) {
    return new ItemLocationReader(locationRepository);
  }

  @Bean("ItemShopReader")
  public ItemShopReader buildItemShopReader(ItemShopRepository itemShopRepository) {
    return new ItemShopReader(itemShopRepository);
  }

  @Bean("ItemSubmissionFactory")
  public ItemSubmissionFactory buildItemSubmissionFactory(UserService userService,
      NotificationService notificationService, ItemListingFactory itemListingFactory,
      ItemLocationReader itemLocationReader, ItemShopReader itemShopReader,
      DiskImageWriter diskImageWriter) {
    Ocr ocr = new OneOcr(List.of(), diskImageWriter);

    return new ItemSubmissionFactory(userService, notificationService, itemListingFactory,
        itemLocationReader, itemShopReader, ocr);
  }

  @Bean("ItemService")
  public ItemService buildItemService(ItemSubmissionFactory itemSubmissionFactory,
      @Qualifier("ItemCsvWriter") ItemCsvWriter itemCsvWriter,
      ScTradeToolsItemPublisher scTradeToolsItemPublisher,
      NotificationService notificationService) {
    return new ItemService(notificationService, itemSubmissionFactory,
        Arrays.asList(itemCsvWriter, scTradeToolsItemPublisher));
  }

  @Bean("CommodityService")
  public CommodityService buildCommodityService(
      CommoditySubmissionFactory commoditySubmissionFactory,
      @Qualifier("CommodityCsvWriter") CommodityCsvWriter commodityCsvLogger,
      ScTradeToolsCommodityPublisher scTradeToolsCommodityPublisher,
      NotificationService notificationService) {
    return new CommodityService(commoditySubmissionFactory,
        Arrays.asList(commodityCsvLogger, scTradeToolsCommodityPublisher), notificationService);
  }

  @Bean
  public SoundUtil buildSoundUtil() {
    return new SoundUtil();
  }

  @Bean("CommodityScreenPrinter")
  public ScreenPrinter buildCommodityScreenPrinter(
      @Qualifier("CommodityService") CommodityService commodityService,
      ImageWriter<Optional<Path>> imageWriter, SoundUtil soundPlayer,
      NotificationService notificationService, SettingRepository settings) {
    List<ImageManipulation> postprocessingManipulations = new ArrayList<>();
    postprocessingManipulations.add(new UpscaleTo4k());

    return new ScreenPrinter(Arrays.asList(commodityService), postprocessingManipulations,
        imageWriter, soundPlayer, notificationService, settings);
  }

  @Bean("ItemScreenPrinter")
  public ScreenPrinter buildItemScreenPrinter(@Qualifier("ItemService") ItemService itemService,
      ImageWriter<Optional<Path>> imageWriter, SoundUtil soundPlayer,
      NotificationService notificationService, SettingRepository settings) {
    List<ImageManipulation> postprocessingManipulations = new ArrayList<>();
    postprocessingManipulations.add(new UpscaleTo4k());

    return new ScreenPrinter(Arrays.asList(itemService), postprocessingManipulations, imageWriter,
        soundPlayer, notificationService, settings);
  }

  @Bean("CommodityKeyListener")
  public KeyListener buildCommodityKeyListener(
      @Qualifier("CommodityScreenPrinter") ScreenPrinter commodityScreenPrinter,
      SettingRepository settingRepository) {
    return new KeyListener(Arrays.asList(commodityScreenPrinter), settingRepository,
        Setting.PRINTSCREEN_COMMODITY_KEYBIND, NativeKeyEvent.VC_F3);
  }

  @Bean("ItemKeyListener")
  public KeyListener buildItemKeyListener(
      @Qualifier("ItemScreenPrinter") ScreenPrinter itemScreenPrinter,
      SettingRepository settingRepository) {
    return new KeyListener(Arrays.asList(itemScreenPrinter), settingRepository,
        Setting.PRINTSCREEN_ITEM_KEYBIND, NativeKeyEvent.VC_F5);
  }

  private String getVersion() {
    return buildProperties == null ? "TEST" : buildProperties.getVersion();
  }
}
