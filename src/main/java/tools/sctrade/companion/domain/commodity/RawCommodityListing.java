package tools.sctrade.companion.domain.commodity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.ocr.LocatedColumn;
import tools.sctrade.companion.utils.StringUtil;

/**
 * Represents a commodity listing that has not been spell checked, nor has its location attached.
 */
public class RawCommodityListing {
  private static final Pattern RIGHT_PATTERN =
      Pattern.compile("([0-9\\.]+) ?scu\\R.*?([0-9\\.km]+)\\/", Pattern.CASE_INSENSITIVE);
  private static final Set<Integer> BOX_SIZES_IN_SCU = Set.of(1, 2, 4, 8, 16, 24, 32);

  private final Logger logger = LoggerFactory.getLogger(RawCommodityListing.class);

  private LocatedColumn left;
  private LocatedColumn right;

  private Optional<String> commodity;
  private Optional<InventoryLevel> inventoryLevel;
  private Optional<Integer> inventory;
  private Optional<Double> price;
  private Optional<List<Integer>> boxSizesInScu;

  RawCommodityListing(LocatedColumn left, LocatedColumn right) {
    this.left = left;
    this.right = right;

    extractCommodity();
    extractInventoryLevel();
    extractInventory();
    extractPrice();
    extractBoxSizesInScu();
  }

  Optional<String> getCommodity() {
    return commodity;
  }

  Optional<InventoryLevel> getInventoryLevel() {
    return inventoryLevel;
  }

  Optional<Integer> getInventory() {
    return inventory;
  }

  Optional<Double> getPrice() {
    return price;
  }

  Optional<List<Integer>> getBoxSizesInScu() {
    return boxSizesInScu;
  }

  boolean isComplete() {
    return commodity.isPresent() && inventoryLevel.isPresent() && inventory.isPresent()
        && price.isPresent() /* && availableBoxSizes.isPresent() not mandatory for now */;
  }

  @Override
  public String toString() {
    String inventory =
        this.inventory.isPresent() ? String.format(Locale.ROOT, "%s SCU", this.inventory.get())
            : "? SCU";
    String commodity = this.commodity.orElse("?");
    String price =
        this.price.isPresent() ? String.format(Locale.ROOT, "¤%f/scu", this.price.get()) : "¤?/scu";
    String inventoryLevel = this.inventoryLevel.isPresent()
        ? String.format(Locale.ROOT, "(%s)", this.inventoryLevel.get().getLabel())
        : "(?)";
    String boxSizesInScu =
        this.boxSizesInScu.isPresent() ? this.boxSizesInScu.get().toString() : "?";

    return String.format(Locale.ROOT, "%s of '%s' for %s %s available in %s", inventory, commodity,
        price, inventoryLevel, boxSizesInScu);
  }

  private void extractCommodity() {
    try {
      var fragments = new ArrayList<>(left.getFragments());
      fragments.remove(fragments.size() - 1);
      String rawCommodity =
          fragments.stream().map(n -> n.getText()).collect(Collectors.joining(" "));
      rawCommodity = rawCommodity.replaceAll("[^a-zA-Z ]", "").strip();
      commodity = Optional.of(rawCommodity);
    } catch (Exception e) {
      logger.debug(String.format(Locale.ROOT, "Could not extract commodity from: %s", left));
      commodity = Optional.empty();
    }
  }

  private void extractInventoryLevel() {
    try {
      var fragments = left.getFragments();
      var fragment = fragments.get(fragments.size() - 1);

      String rawInventoryLevel = fragment.getText();

      var inventoryLevelsByString = Arrays.asList(InventoryLevel.values()).stream()
          .collect(Collectors.toMap(n -> n.getLabel(), n -> n));
      var closestInventoryLevel =
          StringUtil.spellCheck(rawInventoryLevel, inventoryLevelsByString.keySet());
      inventoryLevel = Optional.of(inventoryLevelsByString.get(closestInventoryLevel));
    } catch (Exception e) {
      logger.debug("Could not extract inventory level from: {}", left);
      inventoryLevel = Optional.empty();
    }
  }

  private void extractInventory() {
    try {
      String rightText = getRightText();
      Matcher matcher = RIGHT_PATTERN.matcher(rightText);
      matcher.find();
      String match = matcher.group(1).toLowerCase(Locale.ROOT);
      match = match.replace(".", "");

      inventory = Optional.of(Integer.valueOf(match));
    } catch (Exception e) {
      logger.debug("Could not extract inventory from: {}", right);
      inventory = Optional.empty();
    }
  }

  private void extractPrice() {
    try {
      String rightText = getRightText();
      Matcher matcher = RIGHT_PATTERN.matcher(rightText);
      matcher.find();
      String match = matcher.group(2).toLowerCase(Locale.ROOT);
      boolean isMillions = match.endsWith("m");
      boolean isThousands = match.endsWith("k");
      match = match.replace("m", "").replace("k", "");

      double price = Double.parseDouble(match);
      isThousands = isThousands || (price % 1) > 0; // Decimals = metric notation, which is 99% kilo

      if (isMillions) {
        price *= 1000000;
      } else if (isThousands) {
        price *= 1000;
      }

      this.price = Optional.of(price);
    } catch (Exception e) {
      logger.debug(String.format(Locale.ROOT, "Could not extract price from: %s", right));
      this.price = Optional.empty();
    }
  }

  private void extractBoxSizesInScu() {
    var parsedBoxSizesInScu =
        Arrays.stream(right.getFragments().getLast().getText().strip().split(" ")).map(n -> {
          try {
            return Integer.valueOf(n);
          } catch (Exception e) {
            return 0;
          }
        }).filter(n -> BOX_SIZES_IN_SCU.contains(n)).toList();

    var sortedParsedBoxSizes = new ArrayList<>(parsedBoxSizesInScu);
    Collections.sort(sortedParsedBoxSizes);

    if (parsedBoxSizesInScu.equals(sortedParsedBoxSizes)) {
      this.boxSizesInScu = Optional.of(parsedBoxSizesInScu);
    } else {
      this.boxSizesInScu = Optional.empty();
    }
  }

  private String getRightText() {
    return right.getText().strip().toLowerCase(Locale.ROOT).replace(" ", "").replace(",", ".")
        .replace("i", "1").replace("l", "1").replace("s", "5").replace("$", "5").replace("e", "5")
        .replace("g", "6").replace("b", "8").replace("o", "0").replace("5cu", "scu")
        .replace("5cy", "scu").replace("8cy", "scu").replace("□", "¤").replace("├ñ", "¤");
  }
}
