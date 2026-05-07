package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class UsageTabTest {
  @Test
  void givenUsageTabWhenInitializedThenShowDedicatedInstructionSections() {
    UsageTab usageTab = new UsageTab();

    assertTrue(findLabelTexts(usageTab)
        .containsAll(List.of("Common tips", "Commodity terminals", "Item terminals")));
  }

  @Test
  void givenUsageTabWhenInitializedThenShowFourExampleImages() {
    UsageTab usageTab = new UsageTab();

    assertEquals(4, countLabelsWithIcons(usageTab));
  }

  private List<String> findLabelTexts(Container container) {
    List<String> labelTexts = new ArrayList<>();

    for (Component component : container.getComponents()) {
      if (component instanceof JLabel label && label.getText() != null) {
        labelTexts.add(label.getText());
      }

      if (component instanceof Container child) {
        labelTexts.addAll(findLabelTexts(child));
      }
    }

    return labelTexts;
  }

  private int countLabelsWithIcons(Container container) {
    int count = 0;

    for (Component component : container.getComponents()) {
      if (component instanceof JLabel label && label.getIcon() != null) {
        count++;
      }

      if (component instanceof JPanel panel) {
        count += countLabelsWithIcons(panel);
      } else if (component instanceof Container child) {
        count += countLabelsWithIcons(child);
      }
    }

    return count;
  }
}
