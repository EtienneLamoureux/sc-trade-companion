package tools.sctrade.companion.domain.gamelog;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadShopInventoryDataLogLineProcessor extends GameLogLineProcessor {
  private final Logger logger =
      LoggerFactory.getLogger(LoadShopInventoryDataLogLineProcessor.class);

  public LoadShopInventoryDataLogLineProcessor() {
    this.pattern = Pattern.compile(
        ".+LoadShopInventoryData.+shopName\\[(?<shopName>\\w+)\\] commodityName\\[ResourceType\\.(?<commodityName>\\w+)\\].+boxSize\\[(?<maxBoxSize>\\d+)\\] \\[Team_NAPU\\]\\[Shops\\]\\[UI\\]");
  }

  @Override
  protected void handle(String value) {
    var matcher = pattern.matcher(value);
    var shopName = matcher.group("shopName");
    var commodityName = matcher.group("commodityName");
    var maxBoxSize = Integer.valueOf(matcher.group("maxBoxSize"));

    logger.info("Shop {} sells {} in boxes of up to {} SCU", shopName, commodityName, maxBoxSize);
  }

}
