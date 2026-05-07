package tools.sctrade.companion.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * The usage tab for the companion GUI. This is where users can see the instructions on how to use
 * this app.
 */
public class UsageTab extends JPanel {
  private static final long serialVersionUID = -5302283386634373931L;
  private static final Dimension EXAMPLE_IMAGE_SIZE = new Dimension(320, 180);

  /**
   * Creates a new instance of the usage tab.
   */
  public UsageTab() {
    super(new BorderLayout());

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    content.add(buildTitle("usageTitle"));
    content.add(Box.createVerticalStrut(12));
    content.add(buildSection("usageCommonTipsTitle", "usageCommonTips"));
    content.add(Box.createVerticalStrut(12));
    content.add(buildSection("usageCommodityTitle", "usageCommoditySteps",
        "/images/usage/commodity-good.png", "usageCommodityGoodExample",
        "/images/usage/commodity-bad.png", "usageCommodityBadExample"));
    content.add(Box.createVerticalStrut(12));
    content.add(buildSection("usageItemTitle", "usageItemSteps", "/images/usage/item-good.png",
        "usageItemGoodExample", "/images/usage/item-bad.png", "usageItemBadExample"));
    content.add(Box.createVerticalGlue());

    JScrollPane scrollPane = new JScrollPane(content);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    add(scrollPane, BorderLayout.CENTER);
  }

  private Component buildTitle(String key) {
    JLabel label = new JLabel(LocalizationUtil.get(key));
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    label.putClientProperty("FlatLaf.styleClass", "h1");
    return label;
  }

  private Component buildSection(String titleKey, String stepsKey, String goodImagePath,
      String goodCaptionKey, String badImagePath, String badCaptionKey) {
    JPanel section = new JPanel();
    section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
    section.setAlignmentX(Component.LEFT_ALIGNMENT);
    section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)));

    section.add(buildSectionTitle(titleKey));
    section.add(Box.createVerticalStrut(8));
    section.add(buildBodyLabel(stepsKey));
    section.add(Box.createVerticalStrut(12));
    section.add(buildExamplesPanel(goodImagePath, goodCaptionKey, badImagePath, badCaptionKey));
    return section;
  }

  private Component buildSection(String titleKey, String bodyKey) {
    JPanel section = new JPanel();
    section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
    section.setAlignmentX(Component.LEFT_ALIGNMENT);
    section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)));

    section.add(buildSectionTitle(titleKey));
    section.add(Box.createVerticalStrut(8));
    section.add(buildBodyLabel(bodyKey));
    return section;
  }

  private Component buildSectionTitle(String key) {
    JLabel label = new JLabel(LocalizationUtil.get(key));
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    label.putClientProperty("FlatLaf.styleClass", "h2");
    return label;
  }

  private Component buildBodyLabel(String key) {
    JLabel label = new JLabel(toHtml(LocalizationUtil.get(key)));
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    return label;
  }

  private Component buildExamplesPanel(String goodImagePath, String goodCaptionKey,
      String badImagePath, String badCaptionKey) {
    JPanel examplesPanel = new JPanel(new GridLayout(1, 2, 12, 0));
    examplesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    examplesPanel.add(buildExample(goodImagePath, goodCaptionKey));
    examplesPanel.add(buildExample(badImagePath, badCaptionKey));
    return examplesPanel;
  }

  private Component buildExample(String imagePath, String captionKey) {
    JPanel example = new JPanel();
    example.setLayout(new BoxLayout(example, BoxLayout.Y_AXIS));

    JLabel imageLabel = new JLabel(new ImageIcon(getClass().getResource(imagePath)));
    imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    imageLabel.setBorder(BorderFactory.createLineBorder(example.getForeground()));
    imageLabel.setMaximumSize(EXAMPLE_IMAGE_SIZE);
    imageLabel.setPreferredSize(EXAMPLE_IMAGE_SIZE);
    example.add(imageLabel);
    example.add(Box.createVerticalStrut(6));

    JLabel caption = new JLabel(toHtml(LocalizationUtil.get(captionKey)));
    caption.setAlignmentX(Component.LEFT_ALIGNMENT);
    example.add(caption);
    return example;
  }

  private String toHtml(String value) {
    return String.format(Locale.ROOT, "<html>%s</html>",
        String.join("<br/>", Arrays.asList(value.split("\\R"))));
  }
}
