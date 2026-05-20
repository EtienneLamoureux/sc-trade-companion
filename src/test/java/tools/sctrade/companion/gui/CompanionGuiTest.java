package tools.sctrade.companion.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.RingProgressIndicator;
import java.nio.file.Path;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.sctrade.companion.domain.gamelog.GameLogPathSubject;
import tools.sctrade.companion.domain.setting.Setting;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.User;
import tools.sctrade.companion.domain.user.UserService;
import tools.sctrade.companion.gui.screenshot.ScreenshotRepository;

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
    ScreenshotRepository screenshotRepository = new ScreenshotRepository();

    Stage stage = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      Stage fxStage = new Stage();
      CompanionGui gui = new CompanionGui(userService, gameLogPathSubject, settings,
          screenshotRepository, "1.5.2");
      gui.initialize(fxStage);
      return fxStage;
    });

    assertEquals("SC Trade Companion 1.5.2", stage.getTitle());
    assertEquals(600.0, stage.getWidth());
    assertEquals(575.0, stage.getHeight());

    Scene scene = stage.getScene();
    StackPane stackRoot = assertInstanceOf(StackPane.class, scene.getRoot());
    BorderPane root = companionRoot(stackRoot);
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
    ScreenshotRepository screenshotRepository = new ScreenshotRepository();

    BorderPane root = JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      Stage fxStage = new Stage();
      CompanionGui gui = new CompanionGui(userService, gameLogPathSubject, settings,
          screenshotRepository, "1.5.2");
      gui.initialize(fxStage);
      BorderPane r = companionRoot((StackPane) fxStage.getScene().getRoot());

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

    BorderPane root = companionRoot((StackPane) stage.getScene().getRoot());
    HBox navBar = (HBox) root.getTop();

    assertTrue(navBar.getStyleClass().contains("companion-nav"),
        "Nav bar must have 'companion-nav' style class");
  }

  @Test
  void givenGuiWhenInitializedThenNavLinksHaveCompanionNavLinkStyleClass() {
    Stage stage = buildInitializedStage();

    BorderPane root = companionRoot((StackPane) stage.getScene().getRoot());
    HBox navBar = (HBox) root.getTop();

    navBar.getChildren()
        .forEach(child -> assertTrue(child.getStyleClass().contains("companion-nav-link"),
            child.getId() + " must have 'companion-nav-link' style class"));
  }

  @Test
  void givenGuiWhenInitializedThenUsageLinkIsActive() {
    Stage stage = buildInitializedStage();

    BorderPane root = companionRoot((StackPane) stage.getScene().getRoot());
    HBox navBar = (HBox) root.getTop();

    assertTrue(navBar.getChildren().get(0).getStyleClass().contains("active"),
        "Usage link must be active on initialization");
  }

  @Test
  void givenGuiWhenNavLinkClickedThenClickedLinkBecomesActive() {
    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      Stage fxStage = buildInitializedStage();
      BorderPane root = companionRoot((StackPane) fxStage.getScene().getRoot());
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
      BorderPane root = companionRoot((StackPane) fxStage.getScene().getRoot());
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

  @Test
  void givenGuiWhenInitializedThenClosingModalIsConfiguredAsPersistentCenteredOverlay() {
    Stage stage = buildInitializedStage();

    Scene scene = stage.getScene();
    StackPane rootStack = assertInstanceOf(StackPane.class, scene.getRoot());
    ModalPane modalPane = assertInstanceOf(ModalPane.class, rootStack.lookup("#closingModalPane"));
    assertFalse(modalPane.getPersistent(),
        "Closing modal should be non-persistent until requested");
    assertEquals(Double.MAX_VALUE, modalPane.getMaxWidth(),
        "Closing modal should expand horizontally with window size");
    assertEquals(Double.MAX_VALUE, modalPane.getMaxHeight(),
        "Closing modal should expand vertically with window size");
    assertEquals(null, modalPane.getContent(),
        "Closing modal content should only be created when closing is requested");
  }

  @Test
  void givenGuiWhenCloseRequestedThenClosingModalIsDisplayed() {
    UserService userService = mock(UserService.class);
    when(userService.get()).thenReturn(new User("id", "Pilot"));
    GameLogPathSubject gameLogPathSubject = mock(GameLogPathSubject.class);
    when(gameLogPathSubject.getStarCitizenLivePath()).thenReturn(Optional.of("LIVE"));
    SettingRepository settings = new SettingRepository();
    settings.set(Setting.MY_DATA_PATH, Path.of("my-data"));
    settings.set(Setting.MY_IMAGES_PATH, Path.of("my-images"));
    ScreenshotRepository screenshotRepository = new ScreenshotRepository();

    JavaFxTestUtil.runOnFxThreadAndWait(() -> {
      Stage fxStage = new Stage();
      CompanionGui gui = new CompanionGui(userService, gameLogPathSubject, settings,
          screenshotRepository, "1.5.2") {
        @Override
        protected void requestShutdown() {
          // no-op for test
        }
      };
      gui.initialize(fxStage);

      fxStage.fireEvent(new WindowEvent(fxStage, WindowEvent.WINDOW_CLOSE_REQUEST));

      StackPane stackRoot = (StackPane) fxStage.getScene().getRoot();
      ModalPane modalPane = (ModalPane) stackRoot.lookup("#closingModalPane");
      assertTrue(modalPane.isDisplay(), "Closing modal should be displayed on close request");
      assertTrue(modalPane.getPersistent(), "Closing modal should be persistent while displayed");

      VBox closingContent = assertInstanceOf(VBox.class, modalPane.getContent());
      assertEquals(javafx.geometry.Pos.CENTER, closingContent.getAlignment(),
          "Closing modal content should be centered");
      assertTrue(closingContent.getStyleClass().contains("closing-modal-content"),
          "Closing modal content should use solid background style class");
      assertEquals(420.0, closingContent.getPrefWidth(),
          "Closing modal content should have fixed width");
      assertEquals(220.0, closingContent.getPrefHeight(),
          "Closing modal content should have fixed height");

      Label closingTitle = assertInstanceOf(Label.class, closingContent.lookup("#closingTitle"));
      assertEquals("Closing...", closingTitle.getText());
      assertTrue(closingTitle.getStyleClass().contains("title-2"),
          "Closing label should use title-2 style");

      assertInstanceOf(RingProgressIndicator.class, closingContent.lookup("#closingProgress"));
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
    ScreenshotRepository screenshotRepository = new ScreenshotRepository();

    return JavaFxTestUtil.supplyOnFxThreadAndWait(() -> {
      Stage fxStage = new Stage();
      new CompanionGui(userService, gameLogPathSubject, settings, screenshotRepository, "1.5.2")
          .initialize(fxStage);
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

  private static BorderPane companionRoot(StackPane stackRoot) {
    return stackRoot.getChildren().stream().filter(BorderPane.class::isInstance)
        .map(BorderPane.class::cast).findFirst()
        .orElseThrow(() -> new AssertionError("Companion root BorderPane not found"));
  }
}
