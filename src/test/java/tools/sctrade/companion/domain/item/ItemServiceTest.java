package tools.sctrade.companion.domain.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import tools.sctrade.companion.exceptions.NoCloseStringException;
import tools.sctrade.companion.exceptions.RecoverableProcessingException;

class ItemServiceTest {
  @Test
  void givenNoCloseStringExceptionWhenCheckingTypeThenItIsRecoverable() {
    RuntimeException exception = new NoCloseStringException("sample");

    assertTrue(exception instanceof RecoverableProcessingException);
  }
}
