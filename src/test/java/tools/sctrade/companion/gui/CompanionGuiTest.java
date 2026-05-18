package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.gamelog.GameLogPathSubject;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.User;
import tools.sctrade.companion.domain.user.UserService;

class CompanionGuiTest {
  @BeforeAll
  static void beforeAll() {
    JavaFxTestUtil.startToolkit();
  }

  @Test
  void givenGuiWhenInitializedThenBuildCurrentShellAsJavaFxStage() {
    UserService userService = mock(UserService.class);
    when(userService.get()).thenReturn(new User("id", "Pilot"));
    GameLogPathSubject gameLogPathSubject = mock(GameLogPathSubject.class);
    when(gameLogPathSubject.getStarCitizenLivePath()).thenReturn(Optional.of("LIVE"));
    SettingRepository settings = new SettingRepository();
    settings.set(Setting.MY_DATA_PATH, Path.of("my-data"));
    settings.set(Setting.MY_IMAGES_PATH, Path.of("my-images"));

    Stage stage = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      Stage fxStage = new Stage();
      CompanionGui gui = new CompanionGui(userService, gameLogPathSubject, settings, "1.5.2");
      gui.initialize(fxStage);
      return fxStage;
    });

    assertEquals("SC Trade Companion 1.5.2", stage.getTitle());
    assertEquals(600.0, stage.getWidth());
    assertEquals(575.0, stage.getHeight());

    Scene scene = stage.getScene();
    BorderPane root = assertInstanceOf(BorderPane.class, scene.getRoot());
    assertEquals(1, scene.getStylesheets().size());

    HBox navBar = assertInstanceOf(HBox.class, root.getTop(), "Top nav bar must be an HBox");
    assertEquals(4, navBar.getChildren().size(), "Nav bar must have 4 direct links");
    assertNavLink(navBar, 0, "nav-usage");
    assertNavLink(navBar, 1, "nav-settings");
    assertNavLink(navBar, 2, "nav-screenshots");
    assertNavLink(navBar, 3, "nav-logs");

    assertNotNull(root.getCenter(), "Scene center must not be empty");
    assertInstanceOf(UsageTab.class, root.getCenter(),
        "Default center must be the usage/instructions view");
  }

  @Test
  void givenGuiWhenNavLinksClickedThenCenterViewUpdates() {
    UserService userService = mock(UserService.class);
    when(userService.get()).thenReturn(new User("id", "Pilot"));
    GameLogPathSubject gameLogPathSubject = mock(GameLogPathSubject.class);
    when(gameLogPathSubject.getStarCitizenLivePath()).thenReturn(Optional.of("LIVE"));
    SettingRepository settings = new SettingRepository();
    settings.set(Setting.MY_DATA_PATH, Path.of("my-data"));
    settings.set(Setting.MY_IMAGES_PATH, Path.of("my-images"));

    BorderPane root = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      Stage fxStage = new Stage();
      CompanionGui gui = new CompanionGui(userService, gameLogPathSubject, settings, "1.5.2");
      gui.initialize(fxStage);
      BorderPane r = (BorderPane) fxStage.getScene().getRoot();

      HBox navBar = (HBox) r.getTop();
      clickLink(navBar, "nav-settings");
      assertInstanceOf(SettingsTab.class, r.getCenter(), "Settings link must show SettingsTab");
      clickLink(navBar, "nav-screenshots");
      assertInstanceOf(ScreenshotsTab.class, r.getCenter(),
          "Screenshots link must show ScreenshotsTab");
      clickLink(navBar, "nav-logs");
      assertInstanceOf(LogsTab.class, r.getCenter(), "Logs link must show LogsTab");
      clickLink(navBar, "nav-usage");
      assertInstanceOf(UsageTab.class, r.getCenter(), "Usage link must show UsageTab");

      return r;
    });

    assertNotNull(root);
  }

  @Test
  void givenGuiWhenInitializedThenNavBarHasCompanionNavStyleClass() {
    Stage stage = buildInitializedStage();

    BorderPane root = (BorderPane) stage.getScene().getRoot();
    HBox navBar = (HBox) root.getTop();

    assertTrue(navBar.getStyleClass().contains("companion-nav"),
        "Nav bar must have 'companion-nav' style class");
  }

  @Test
  void givenGuiWhenInitializedThenNavLinksHaveCompanionNavLinkStyleClass() {
    Stage stage = buildInitializedStage();

    BorderPane root = (BorderPane) stage.getScene().getRoot();
    HBox navBar = (HBox) root.getTop();

    navBar.getChildren()
        .forEach(child -> assertTrue(child.getStyleClass().contains("companion-nav-link"),
            child.getId() + " must have 'companion-nav-link' style class"));
  }

  @Test
  void givenGuiWhenInitializedThenUsageLinkIsActive() {
    Stage stage = buildInitializedStage();

    BorderPane root = (BorderPane) stage.getScene().getRoot();
    HBox navBar = (HBox) root.getTop();

    assertTrue(navBar.getChildren().get(0).getStyleClass().contains("active"),
        "Usage link must be active on initialization");
  }

  @Test
  void givenGuiWhenNavLinkClickedThenClickedLinkBecomesActive() {
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      Stage fxStage = buildInitializedStage();
      BorderPane root = (BorderPane) fxStage.getScene().getRoot();
      HBox navBar = (HBox) root.getTop();

      clickLink(navBar, "nav-settings");

      assertTrue(findLink(navBar, "nav-settings").getStyleClass().contains("active"),
          "Settings link must become active after click");
      assertFalse(findLink(navBar, "nav-usage").getStyleClass().contains("active"),
          "Usage link must become inactive after settings is clicked");
    });
  }

  @Test
  void givenGuiWhenActiveNavLinkClickedThenContentDoesNotFlash() {
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      Stage fxStage = buildInitializedStage();
      BorderPane root = (BorderPane) fxStage.getScene().getRoot();
      HBox navBar = (HBox) root.getTop();

      var initialCenter = root.getCenter();
      assertEquals(1.0, initialCenter.getOpacity(), "Default content should start fully visible");

      clickLink(navBar, "nav-usage");

      assertTrue(initialCenter == root.getCenter(),
          "Clicking the active nav link should keep the same center node");
      assertEquals(1.0, root.getCenter().getOpacity(),
          "Clicking the active nav link should not restart fade from opacity 0");
    });
  }

  private Stage buildInitializedStage() {
    UserService userService = mock(UserService.class);
    when(userService.get()).thenReturn(new User("id", "Pilot"));
    GameLogPathSubject gameLogPathSubject = mock(GameLogPathSubject.class);
    when(gameLogPathSubject.getStarCitizenLivePath()).thenReturn(Optional.of("LIVE"));
    SettingRepository settings = new SettingRepository();
    settings.set(Setting.MY_DATA_PATH, Path.of("my-data"));
    settings.set(Setting.MY_IMAGES_PATH, Path.of("my-images"));

    return JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      Stage fxStage = new Stage();
      new CompanionGui(userService, gameLogPathSubject, settings, "1.5.2").initialize(fxStage);
      return fxStage;
    });
  }

  private static Hyperlink findLink(HBox navBar, String id) {
    return navBar.getChildren().stream().filter(n -> id.equals(n.getId())).map(n -> (Hyperlink) n)
        .findFirst().orElseThrow(() -> new AssertionError("Nav link not found: " + id));
  }

  private static void assertNavLink(HBox navBar, int index, String expectedId) {
    assertInstanceOf(Hyperlink.class, navBar.getChildren().get(index),
        "Nav item " + index + " must be a Hyperlink");
    assertEquals(expectedId, navBar.getChildren().get(index).getId(),
        "Nav item " + index + " must have id '" + expectedId + "'");
  }

  private static void clickLink(HBox navBar, String id) {
    navBar.getChildren().stream().filter(n -> id.equals(n.getId())).map(n -> (Hyperlink) n)
        .findFirst().orElseThrow(() -> new AssertionError("Nav link not found: " + id)).fire();
  }
}
