package tools.sctrade.companion.domain.commodity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.sctrade.companion.domain.ocr.LocatedColumn;

public class RawCommodityListing {
  private static final Pattern RIGHT_PATTERN = Pattern
      .compile("\\D*([0-9\\,]+).+\\R\\D*((\\d+[\\.\\,])?\\d+[k ]*)", Pattern.CASE_INSENSITIVE);

  private final Logger logger = LoggerFactory.getLogger(RawCommodityListing.class);

  private LocatedColumn left;
  private LocatedColumn right;

  private Optional<String> commodity;
  private Optional<Integer> quantity;
  private Optional<Double> price;

  RawCommodityListing(LocatedColumn left, LocatedColumn right) {
    this.left = left;
    this.right = right;

    extractQuantity();
    extractPrice();
    extractCommodity();
  }

  Optional<String> getCommodity() {
    return commodity;
  }

  Optional<Integer> getQuantity() {
    return quantity;
  }

  Optional<Double> getPrice() {
    return price;
  }

  @Override
  public String toString() {
    String quantity =
        this.quantity.isPresent() ? String.format(Locale.ROOT, "%s SCU", this.quantity.get())
            : "? SCU";
    String commodity = this.commodity.orElse("?");
    String price = this.price.isPresent() ? String.format(Locale.ROOT, "¤%f/unit", this.price.get())
        : "¤?/unit";

    return String.format(Locale.ROOT, "%s of '%s' for %s", quantity, commodity, price);
  }

  private void extractCommodity() {
    try {
      var fragments = new ArrayList<>(left.getFragments());
      fragments.remove(fragments.size() - 1);
      commodity =
          Optional.of(fragments.stream().map(n -> n.getText()).collect(Collectors.joining(" ")));
    } catch (Exception e) {
      logger.warn(String.format(Locale.ROOT, "Could not extract commodity from: %s", left));
      commodity = Optional.empty();
    }
  }

  private void extractQuantity() {
    try {
      Matcher matcher = RIGHT_PATTERN.matcher(right.getText().replace(" ", ""));
      matcher.find();
      String match = matcher.group(1).toLowerCase();
      match = match.replace(",", "");

      quantity = Optional.of(Integer.valueOf(match));
    } catch (Exception e) {
      logger.warn(String.format(Locale.ROOT, "Could not extract quantity from: %s", right));
      quantity = Optional.empty();
    }
  }

  private void extractPrice() {
    try {
      Matcher matcher = RIGHT_PATTERN.matcher(right.getText().replace(" ", ""));
      matcher.find();
      String match = matcher.group(2).toLowerCase();
      boolean isThousands = match.endsWith("k");
      match = match.replace("k", "");

      double price = Double.parseDouble(match);

      if (price >= 1000.0) {
        /*
         * Prices never have more than 3 digits before the decimal character, as the 'k' notation is
         * preferred. This indicates the '¤' character was read as a number: it must be stripped.
         */
        price %= 1000;
      }

      if (isThousands) {
        price *= 1000;
      }

      this.price = Optional.of(price);
    } catch (Exception e) {
      logger.warn(String.format(Locale.ROOT, "Could not extract price from: %s", right));
      this.price = Optional.empty();
    }
  }
}
