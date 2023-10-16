package tools.sctrade.companion.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.sctrade.companion.domain.commodity.CommodityPublisher;
import tools.sctrade.companion.domain.commodity.CommodityService;
import tools.sctrade.companion.domain.image.ImageManipulation;
import tools.sctrade.companion.domain.image.ImageProcessor;
import tools.sctrade.companion.domain.image.manipulations.ConvertToGreyscale;
import tools.sctrade.companion.domain.image.manipulations.InvertColors;
import tools.sctrade.companion.domain.image.manipulations.ScaleAndOffsetColors;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.input.KeyListener;
import tools.sctrade.companion.input.ScreenPrinter;
import tools.sctrade.companion.ocr.tesseract.CommodityTesseractOcr;
import tools.sctrade.companion.output.commodity.CommodityCsvWriter;
import tools.sctrade.companion.output.commodity.ScTradeToolsClient;
import tools.sctrade.companion.swing.CompanionGui;

@Configuration
public class AppConfig {
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

  @Bean("CommodityTesseractOcr")
  public CommodityTesseractOcr buildCommodityTesseractOcr() {
    List<ImageManipulation> preprocessingManipulations = new ArrayList<>();
    preprocessingManipulations.add(new ConvertToGreyscale());
    preprocessingManipulations.add(new InvertColors());
    preprocessingManipulations.add(new ScaleAndOffsetColors(10.0f, 0.0f));

    return new CommodityTesseractOcr(preprocessingManipulations);
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
