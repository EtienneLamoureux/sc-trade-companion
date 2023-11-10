package tools.sctrade.companion.spring;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.sctrade.companion.domain.commodity.CommodityListingsTesseractOcr;
import tools.sctrade.companion.domain.commodity.CommodityLocationTesseractOcr;
import tools.sctrade.companion.domain.commodity.CommodityService;
import tools.sctrade.companion.domain.commodity.CommoditySubmissionFactory;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageWriter;
import tools.sctrade.companion.domain.image.manipulations.AdjustBrightnessAndContrast;
import tools.sctrade.companion.domain.image.manipulations.ConvertToGreyscale;
import tools.sctrade.companion.domain.image.manipulations.InvertColors;
import tools.sctrade.companion.domain.image.manipulations.UpscaleTo4k;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.user.Setting;
import tools.sctrade.companion.domain.user.SettingRepository;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.input.KeyListener;
import tools.sctrade.companion.input.ScreenPrinter;
import tools.sctrade.companion.output.DiskImageWriter;
import tools.sctrade.companion.output.commodity.CommodityCsvWriter;
import tools.sctrade.companion.output.commodity.ScTradeToolsClient;
import tools.sctrade.companion.swing.CompanionGui;

@Configuration
public class AppConfig {
  @Value("${output.screenshots:true}")
  private String outputScreenshots;
  @Value("${output.intermediary-images:false}")
  private String outputIntermediaryImages;

  @Bean("SettingRepository")
  public SettingRepository buildSettingRepository() {
    var settingRepository = new SettingRepository();
    settingRepository.set(Setting.MY_IMAGES_PATH,
        Paths.get("..", "my-images").normalize().toAbsolutePath());
    settingRepository.set(Setting.MY_DATA_PATH,
        Paths.get("..", "my-data").normalize().toAbsolutePath());
    settingRepository.set(Setting.OUTPUT_SCREENSHOTS, outputScreenshots);
    settingRepository.set(Setting.OUTPUT_TRANSIENT_IMAGES, outputIntermediaryImages);

    return settingRepository;
  }

  @Bean("CompanionGui")
  public CompanionGui buildCompanionGui() {
    return new CompanionGui();
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
  public ScTradeToolsClient buildScTradeToolsClient() {
    return new ScTradeToolsClient();
  }

  @Bean("CommodityListingsTesseractOcr")
  public CommodityListingsTesseractOcr buildCommodityTesseractOcr() {
    List<ImageManipulation> preprocessingManipulations = new ArrayList<>();
    preprocessingManipulations.add(new ConvertToGreyscale());
    preprocessingManipulations.add(new InvertColors());
    preprocessingManipulations.add(new AdjustBrightnessAndContrast(10.0f, 0.0f));

    return new CommodityListingsTesseractOcr(preprocessingManipulations);
  }

  @Bean("CommodityLocationTesseractOcr")
  public CommodityLocationTesseractOcr buildCommodityLocationTesseractOcr() {
    List<ImageManipulation> preprocessingManipulations = new ArrayList<>();
    preprocessingManipulations.add(new ConvertToGreyscale());
    preprocessingManipulations.add(new InvertColors());
    preprocessingManipulations.add(new AdjustBrightnessAndContrast(10.0f, 0.0f));

    return new CommodityLocationTesseractOcr(preprocessingManipulations);
  }

  @Bean("DiskImageWriter")
  public DiskImageWriter buildDiskImageWriter(SettingRepository settingRepository) {
    return new DiskImageWriter(settingRepository);
  }

  @Bean("CommoditySubmissionFactory")
  public CommoditySubmissionFactory buildCommoditySubmissionFactory(UserService userService,
      @Qualifier("CommodityListingsTesseractOcr") Ocr listingsOcr,
      @Qualifier("CommodityLocationTesseractOcr") Ocr locationOcr, ImageWriter imageWriter) {
    return new CommoditySubmissionFactory(userService, listingsOcr, locationOcr, imageWriter);
  }

  @Bean("CommodityService")
  public CommodityService buildCommodityService(
      CommoditySubmissionFactory commoditySubmissionFactory,
      @Qualifier("CommodityCsvWriter") CommodityCsvWriter commodityCsvLogger,
      @Qualifier("ScTradeToolsClient") ScTradeToolsClient scTradeToolsClient) {
    return new CommodityService(commoditySubmissionFactory,
        Arrays.asList(commodityCsvLogger, scTradeToolsClient));
  }

  @Bean("ScreenPrinter")
  public ScreenPrinter buildScreenPrinter(
      @Qualifier("CommodityService") CommodityService commodityService, ImageWriter imageWriter) {
    List<ImageManipulation> postprocessingManipulations = new ArrayList<>();
    postprocessingManipulations.add(new UpscaleTo4k());

    return new ScreenPrinter(Arrays.asList(commodityService), postprocessingManipulations,
        imageWriter);
  }

  @Bean("KeyListener")
  public KeyListener buildNativeKeyListener(@Qualifier("ScreenPrinter") Runnable screenPrinter) {
    return new KeyListener(Arrays.asList(screenPrinter));
  }

}
