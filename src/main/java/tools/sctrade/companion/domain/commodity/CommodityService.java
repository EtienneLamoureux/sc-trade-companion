package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import tools.sctrade.companion.domain.image.ImageProcessor;
import tools.sctrade.companion.domain.ocr.Ocr;
import tools.sctrade.companion.domain.ocr.OcrResult;
import tools.sctrade.companion.domain.user.UserService;

public class CommodityService extends ImageProcessor {
  private UserService userService;
  private Ocr ocr;
  private Collection<CommodityPublisher> outputAdapters;
  private final String SEARCH = "([0-9])[\\.\\, ]{2,}([0-9])";
  private final String SEARCH2 = "$1\\.$2";
  private final String SEARCH3 =
      "([a-z ]+) ([0-9,]+ |os).+\\R(.+) [^0-9]?(([0-9]{1,3}[\\.\\,])?[0-9]+[k ]+?)\\/";

  public CommodityService(UserService userService, Ocr ocr,
      Collection<CommodityPublisher> outputAdapters) {
    this.userService = userService;
    this.ocr = ocr;
    this.outputAdapters = outputAdapters;
  }

  @Override
  public void process(BufferedImage screenCapture) {
    OcrResult result = ocr.read(screenCapture);

    publish(Collections.emptyList()); // TODO
  }

  private void publish(Collection<CommodityListing> listings) {
    var user = userService.get();
    var submission = new CommoditySubmission(user, listings);

    outputAdapters.stream().forEach(n -> n.publishAsynchronously(submission));
  }
}
