package tools.sctrade.companion.domain.commodity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import tools.sctrade.companion.exceptions.NoListingsException;
import tools.sctrade.companion.exceptions.RecoverableProcessingException;

class CommodityServiceTest {
  @Test
  void givenNoListingsExceptionWhenCheckingTypeThenItIsRecoverable() {
    RuntimeException exception = new NoListingsException();

    assertTrue(exception instanceof RecoverableProcessingException);
  }
}
