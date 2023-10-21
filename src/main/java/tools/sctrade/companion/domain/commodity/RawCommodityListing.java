package tools.sctrade.companion.domain.commodity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.sctrade.companion.domain.ocr.LocatedColumn;

class RawCommodityListing {
  private static final Pattern RIGHT_PATTERN =
      Pattern.compile("(?>.+)?([0-9\\,]).+\\R(?>[^0-9]+)?(([0-9]+[\\.\\,])?[0-9]+([kK ]+)?)");
  private LocatedColumn left;
  private LocatedColumn right;

  RawCommodityListing(LocatedColumn left, LocatedColumn right) {
    this.left = left;
    this.right = right;
  }

  String getCommodity() {
    return "";
  }

  int getQuantity() {
    Matcher matcher = RIGHT_PATTERN.matcher(right.getText());
    String match = matcher.group(1).toLowerCase();
    match = match.replace(",", "");

    return Integer.valueOf(match);
  }

  public double getPrice() {
    Matcher matcher = RIGHT_PATTERN.matcher(right.getText());
    String match = matcher.group(2).toLowerCase();
    boolean isThousands = match.endsWith("k");
    match = match.replace("k", "");

    double price = Double.parseDouble(match);

    if (isThousands) {
      price *= 1000;
    }

    return price;
  }
}
