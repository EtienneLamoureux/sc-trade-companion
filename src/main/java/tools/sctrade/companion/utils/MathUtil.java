package tools.sctrade.companion.utils;

import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * A utility class that provides methods for mathematical operations.
 */
public class MathUtil {
  /**
   * Calculates the outliers in a list of values using the IQR method.
   *
   * @param values The list of values to calculate the outliers from.
   * @return A list of the outliers in the values.
   */
  public static List<Double> calculateOuliers(List<Double> values) {
    DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
    values.stream().forEach(n -> descriptiveStatistics.addValue(n));

    double q1 = descriptiveStatistics.getPercentile(25);
    double q3 = descriptiveStatistics.getPercentile(75);
    double interQuartileRange = q3 - q1;
    double lowerFence = q1 - (1.5 * interQuartileRange);
    double upperFence = q3 + (1.5 * interQuartileRange);

    return values.stream().filter(n -> n < lowerFence || n > upperFence).toList();
  }

  /**
   * Calculates the mean of a distribution.
   *
   * @param distribution The distribution to calculate the mean from.
   * @return The mean of the distribution.
   */
  public static double calculateMean(List<Double> distribution) {
    DescriptiveStatistics statistics = new DescriptiveStatistics();
    distribution.stream().forEach(n -> statistics.addValue(n));

    return statistics.getMean();
  }
}
