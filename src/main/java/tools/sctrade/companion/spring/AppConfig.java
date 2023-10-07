package tools.sctrade.companion.spring;

import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.sctrade.companion.domain.commodity.CommodityOutputAdapter;
import tools.sctrade.companion.domain.commodity.CommodityService;
import tools.sctrade.companion.input.KeyListener;
import tools.sctrade.companion.input.ScreenPrinter;
import tools.sctrade.companion.output.commodity.FileCommodityOutputAdapter;
import tools.sctrade.companion.output.commodity.ScTradeToolsCommodityOutputAdapter;
import tools.sctrade.companion.swing.CompanionGui;

@Configuration
public class AppConfig {
  @Bean
  public CompanionGui buildCompanionGui() {
    return new CompanionGui();
  }

  @Bean("ScreenPrinter")
  public Runnable buildScreenPrinter() {
    return new ScreenPrinter();
  }

  @Bean
  public NativeKeyListener buildNativeKeyListener(
      @Qualifier("ScreenPrinter") Runnable screenPrinter) {
    return new KeyListener(Arrays.asList(screenPrinter));
  }

  @Bean("ScTradeToolsCommodityOutputAdapter")
  public CommodityOutputAdapter buildScTradeToolsCommodityOutputAdapter() {
    return new ScTradeToolsCommodityOutputAdapter();
  }

  @Bean("FileCommodityOutputAdapter")
  public CommodityOutputAdapter buildFileCommodityOutputAdapter() {
    return new FileCommodityOutputAdapter();
  }

  @Bean
  public CommodityService buildCommodityService(
      @Qualifier("FileCommodityOutputAdapter") CommodityOutputAdapter fileAdapter,
      @Qualifier("ScTradeToolsCommodityOutputAdapter") CommodityOutputAdapter scTradeToolsAdapter) {
    return new CommodityService(Arrays.asList(fileAdapter, scTradeToolsAdapter));
  }
}
