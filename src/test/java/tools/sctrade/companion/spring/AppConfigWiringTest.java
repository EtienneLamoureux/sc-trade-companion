package tools.sctrade.companion.spring;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import tools.sctrade.companion.CompanionApplication;
import tools.sctrade.companion.domain.commodity.CommoditySubmissionFactory;
import tools.sctrade.companion.domain.item.ItemSubmissionFactory;
import tools.sctrade.companion.input.ScreenPrinter;
import tools.sctrade.companion.utils.Processor;

class AppConfigWiringTest {

  @Test
  void givenCommodityScreenPrinterWhenContextLoadsThenItUsesCommodityScreenshotSubmissionFacade()
      throws Exception {
    try (ConfigurableApplicationContext context =
        new SpringApplicationBuilder(CompanionApplication.class, NativeOcrTestConfig.class)
            .headless(false).web(WebApplicationType.NONE)
            .properties("spring.main.allow-bean-definition-overriding=true")
            .run("--spring.main.banner-mode=off")) {
      ScreenPrinter screenPrinter = context.getBean("CommodityScreenPrinter", ScreenPrinter.class);
      Processor<?> facade = context.getBean("CommodityScreenshotSubmissionFacade", Processor.class);

      assertSame(facade, readProcessor(screenPrinter));
    }
  }

  @Test
  void givenItemScreenPrinterWhenContextLoadsThenItUsesItemScreenshotSubmissionFacade()
      throws Exception {
    try (ConfigurableApplicationContext context =
        new SpringApplicationBuilder(CompanionApplication.class, NativeOcrTestConfig.class)
            .headless(false).web(WebApplicationType.NONE)
            .properties("spring.main.allow-bean-definition-overriding=true")
            .run("--spring.main.banner-mode=off")) {
      ScreenPrinter screenPrinter = context.getBean("ItemScreenPrinter", ScreenPrinter.class);
      Processor<?> facade = context.getBean("ItemScreenshotSubmissionFacade", Processor.class);

      assertSame(facade, readProcessor(screenPrinter));
    }
  }

  @SuppressWarnings("unchecked")
  private Processor<BufferedImage> readProcessor(ScreenPrinter screenPrinter) throws Exception {
    Field processor = ScreenPrinter.class.getDeclaredField("processor");
    processor.setAccessible(true);
    return (Processor<BufferedImage>) processor.get(screenPrinter);
  }

  @TestConfiguration
  static class NativeOcrTestConfig {
    @Bean("RawCommoditySubmissionFactory")
    CommoditySubmissionFactory buildRawCommoditySubmissionFactory() {
      return mock(CommoditySubmissionFactory.class);
    }

    @Bean("RawItemSubmissionFactory")
    ItemSubmissionFactory buildRawItemSubmissionFactory() {
      return mock(ItemSubmissionFactory.class);
    }
  }
}
