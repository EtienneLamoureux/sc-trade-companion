package tools.sctrade.companion.spring;

import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.sctrade.companion.domain.ImageProcessor;
import tools.sctrade.companion.domain.commodity.CommodityPublisher;
import tools.sctrade.companion.domain.commodity.CommodityService;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.input.KeyListener;
import tools.sctrade.companion.input.ScreenPrinter;
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

  @Bean("CommodityService")
  public CommodityService buildCommodityService(UserService userService,
      @Qualifier("CommodityCsvWriter") CommodityPublisher commodityCsvLogger,
      @Qualifier("ScTradeToolsClient") CommodityPublisher scTradeToolsClient) {
    return new CommodityService(userService, Arrays.asList(commodityCsvLogger, scTradeToolsClient));
  }

  @Bean("ScreenPrinter")
  public ScreenPrinter buildScreenPrinter(
      @Qualifier("CommodityService") ImageProcessor commodityService) {
    return new ScreenPrinter(Arrays.asList(commodityService));
  }

  @Bean("NativeKeyListener")
  public NativeKeyListener buildNativeKeyListener(
      @Qualifier("ScreenPrinter") Runnable screenPrinter) {
    return new KeyListener(Arrays.asList(screenPrinter));
  }

}
