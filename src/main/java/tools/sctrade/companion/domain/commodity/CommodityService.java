package tools.sctrade.companion.domain.commodity;

import java.awt.image.BufferedImage;
import java.util.Collection;
import tools.sctrade.companion.domain.ImageProcessor;
import tools.sctrade.companion.domain.user.UserService;

public class CommodityService implements ImageProcessor {
  private UserService userService;
  private Collection<CommodityPublisher> outputAdapters;

  public CommodityService(UserService userService, Collection<CommodityPublisher> outputAdapters) {
    this.userService = userService;
    this.outputAdapters = outputAdapters;
  }

  @Override
  public void processAsynchronously(BufferedImage screenCapture) {

  }

  private void publish(Collection<CommodityListing> listings) {
    var user = userService.get();
    var submission = new CommoditySubmission(user, listings);

    outputAdapters.stream().forEach(n -> n.publishAsynchronously(submission));
  }
}
