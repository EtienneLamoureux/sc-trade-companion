package tools.sctrade.companion.spring;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.sctrade.companion.domain.commodity.CommodityPublisher;
import tools.sctrade.companion.domain.commodity.CommodityService;
import tools.sctrade.companion.domain.commodity.CommodityTesseractOcr;
import tools.sctrade.companion.domain.image.ImageProcessor;
import tools.sctrade.companion.domain.image.Ocr;
import tools.sctrade.companion.domain.image.OcrConfiguration;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.input.KeyListener;
import tools.sctrade.companion.input.ScreenPrinter;
import tools.sctrade.companion.output.commodity.CommodityCsvWriter;
import tools.sctrade.companion.output.commodity.ScTradeToolsClient;
import tools.sctrade.companion.swing.CompanionGui;

@Configuration
public class AppConfig {
  @Value("${orc.configuration.greyscale:false}")
  private boolean convertToGreyscale;
  @Value("${orc.configuration.invert:false}")
  private boolean invertColors;
  @Value("${orc.configuration.brightness:0.0}")
  private float brightnessFactor;
  @Value("${orc.configuration.contrast:0}")
  private int contrastOffset;

  @Bean("CompanionGui")
  public CompanionGui buildCompanionGui() {
    return new CompanionGui();
  }

  @Bean("UserService")
  public UserService buildUserService() {
    return new UserService();
  }

  @Bean("CommodityCsvWriter")
  public CommodityCsvWriter buildCommodityCsvLogger() {
    return new CommodityCsvWriter();
  }

  @Bean("ScTradeToolsClient")
  public ScTradeToolsClient buildScTradeToolsClient() {
    return new ScTradeToolsClient();
  }

  @Bean("OcrConfiguration")
  public OcrConfiguration buildOcrConfiguration() {
    return new OcrConfiguration(convertToGreyscale, invertColors, brightnessFactor, contrastOffset);
  }

  @Bean("CommodityTesseractOcr")
  public CommodityTesseractOcr buildCommodityTesseractOcr(OcrConfiguration configuration) {
    return new CommodityTesseractOcr(configuration);
  }

  @Bean("CommodityService")
  public CommodityService buildCommodityService(UserService userService,
      @Qualifier("CommodityTesseractOcr") Ocr ocr,
      @Qualifier("CommodityCsvWriter") CommodityPublisher commodityCsvLogger,
      @Qualifier("ScTradeToolsClient") CommodityPublisher scTradeToolsClient) {
    return new CommodityService(userService, ocr,
        Arrays.asList(commodityCsvLogger, scTradeToolsClient));
  }

  @Bean("ScreenPrinter")
  public ScreenPrinter buildScreenPrinter(
      @Qualifier("CommodityService") ImageProcessor commodityService) {
    return new ScreenPrinter(Arrays.asList(commodityService));
  }

  @Bean("KeyListener")
  public KeyListener buildNativeKeyListener(@Qualifier("ScreenPrinter") Runnable screenPrinter) {
    return new KeyListener(Arrays.asList(screenPrinter));
  }

}
