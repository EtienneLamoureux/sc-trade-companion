package tools.sctrade.companion.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.Test;

class LocalizationBundleIntegrityTest {
  private static final String BUNDLE_BASE_NAME = "bundles.localization";
  private static final List<String> BUNDLE_FILES =
      List.of("localization.properties", "localization_de.properties", "localization_fr.properties",
          "localization_pt.properties", "localization_sp.properties");
  private static final List<Locale> SUPPORTED_LOCALES = List.of(Locale.ENGLISH, Locale.GERMAN,
      Locale.FRENCH, Locale.forLanguageTag("pt"), Locale.forLanguageTag("sp"));

  @Test
  void givenLocalizationFilesWhenDecodingAsUtf8ThenEachFileIsValidUtf8() {
    for (String fileName : BUNDLE_FILES) {
      byte[] bytes = readResourceFile(fileName);
      try {
        StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes));
      } catch (CharacterCodingException e) {
        fail("Expected valid UTF-8 bundle file but found invalid bytes in " + fileName, e);
      }
    }
  }

  @Test
  void givenTooltipStarCitizenLivePathWhenReadingAllLocalesThenNoHtmlMarkupIsPresent() {
    for (Locale locale : SUPPORTED_LOCALES) {
      String tooltipValue = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale)
          .getString("tooltipStarCitizenLivePath");
      assertFalse(tooltipValue.contains("<html>"),
          "tooltipStarCitizenLivePath must not contain HTML");
      assertFalse(tooltipValue.contains("<body"),
          "tooltipStarCitizenLivePath must not contain HTML");
      assertFalse(tooltipValue.contains("<br"), "tooltipStarCitizenLivePath must not contain HTML");
    }
  }

  @Test
  void givenUsageInstructionsWhenReadingNonEnglishLocalesThenNoMojibakeCharactersArePresent() {
    for (Locale locale : List.of(Locale.GERMAN, Locale.FRENCH, Locale.forLanguageTag("pt"),
        Locale.forLanguageTag("sp"))) {
      ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
      String commodities = bundle.getString("usageInstructionsCommodities");
      String gearComponents = bundle.getString("usageInstructionsGearComponents");
      assertNoMojibake(commodities);
      assertNoMojibake(gearComponents);
    }
  }

  private byte[] readResourceFile(String fileName) {
    try (InputStream resourceStream = getClass().getResourceAsStream("/bundles/" + fileName)) {
      assertNotNull(resourceStream, "Expected bundle file to exist: " + fileName);
      return resourceStream.readAllBytes();
    } catch (IOException e) {
      throw new AssertionError("Unable to read bundle file " + fileName, e);
    }
  }

  private void assertNoMojibake(String text) {
    assertFalse(text.contains("�"),
        "Localized instructions should not include replacement character");
    assertFalse(text.contains("\u009d\u008c"),
        "Localized instructions should not include mojibake bytes");
  }
}
