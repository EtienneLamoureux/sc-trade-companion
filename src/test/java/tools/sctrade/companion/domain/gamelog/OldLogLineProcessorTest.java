package tools.sctrade.companion.domain.gamelog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OldLogLineProcessorTest {
  private OldLogLineProcessor oldLogLineProcessor;

  @BeforeEach
  void setUp() {
    oldLogLineProcessor = new OldLogLineProcessor();
  }

  @Test
  void givenOldLogLineWhenCheckingIfCanHandleThenReturnTrue() {
    var canHandle = oldLogLineProcessor
        .canHandle("<2024-11-13T15:01:11.102Z> Requesting game mode Frontend_Main/SC_Frontend");

    assertTrue(canHandle);
  }

  @Test
  void givenNewLogLineWhenCheckingIfCanHandleThenReturnFalse() {
    var canHandle = oldLogLineProcessor.canHandle(
        "<2124-11-13T15:01:10.344Z> [Notice] <BeginAsyncReset> CEntitySystem::BeginAsyncReset called! Set m_bResetting = true [Team_EntitySystemTech][Entity][EntitySystem]");

    assertFalse(canHandle);
  }

  @Test
  void givenInvalidLogLineWhenCheckingIfCanHandleThenReturnFalse() {
    var canHandle = oldLogLineProcessor.canHandle("bonjour");

    assertFalse(canHandle);
  }
}
