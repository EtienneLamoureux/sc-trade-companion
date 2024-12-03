package tools.sctrade.companion.domain.gamelog.lineprocessors;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.gamelog.GameLogLineProcessor;

public class LoadShopInventoryDataLogLineProcessor extends GameLogLineProcessor {
  private final Logger logger =
      LoggerFactory.getLogger(LoadShopInventoryDataLogLineProcessor.class);

  public LoadShopInventoryDataLogLineProcessor() {
    this.regex =
        ".+LoadShopInventoryData.+shopName\\[(?<shopName>\\w+)\\] commodityName\\[ResourceType\\.(?<commodityName>\\w+)\\].+boxSize\\[(?<maxBoxSize>\\d+)\\] \\[Team_NAPU\\]\\[Shops\\]\\[UI\\]";
  }

  @Override
  protected void handle(String value) {
    var pattern = Pattern.compile(regex);
    var matcher = pattern.matcher(value);
    matcher.matches();

    var shopName = matcher.group("shopName");
    var commodityName = matcher.group("commodityName");
    var maxBoxSize = Integer.valueOf(matcher.group("maxBoxSize"));

    logger.info("Shop {} sells {} in boxes of up to {} SCU", shopName, commodityName, maxBoxSize);
  }

}
