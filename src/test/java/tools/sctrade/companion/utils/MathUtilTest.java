package tools.sctrade.companion.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MathUtilTest {
  @Test
  void givenDistributionWithLongTailWhenCalculatingOuliersThenReturnTail() {
    List<Double> distribution = Arrays.asList(12.0, 13.0, 14.0, 14.0, 14.0, 15.0, 15.0, 16.0, 16.0,
        57.0, 57.0, 58.0, 58.0, 58.0, 58.0, 58.0, 206.0);
    var outliers = MathUtil.calculateOuliers(distribution);

    assertEquals(Arrays.asList(206.0), outliers);
  }

  @Test
  void givenSmallDistributionWhenCalculatingOuliersThenReturnEmptyList() {
    List<Double> distribution = Arrays.asList(1.0);
    var outliers = MathUtil.calculateOuliers(distribution);

    assertTrue(outliers.isEmpty());
  }
}
