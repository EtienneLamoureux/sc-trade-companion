package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import tools.sctrade.companion.domain.image.ImageProcessor;
import tools.sctrade.companion.domain.image.Ocr;
import tools.sctrade.companion.domain.user.UserService;

public class CommodityService extends ImageProcessor {
  private UserService userService;
  private Ocr ocr;
  private Collection<CommodityPublisher> outputAdapters;

  public CommodityService(UserService userService, Ocr ocr,
      Collection<CommodityPublisher> outputAdapters) {
    this.userService = userService;
    this.ocr = ocr;
    this.outputAdapters = outputAdapters;
  }

  @Override
  public void process(BufferedImage screenCapture) {
    String text = ocr.read(screenCapture);
  }

  private void publish(Collection<CommodityListing> listings) {
    var user = userService.get();
    var submission = new CommoditySubmission(user, listings);

    outputAdapters.stream().forEach(n -> n.publishAsynchronously(submission));
  }
}
