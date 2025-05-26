package tools.sctrade.companion.domain.user.idgenerators;

import java.util.UUID;
import tools.sctrade.companion.domain.user.UserIdGenerator;

/**
 * Ultimate fallback to generate a user id. Will cause a new id to be generated for each session.
 */
public class RandomUserIdGenerator extends UserIdGenerator {
  @Override
  protected String generateId() {
    return UUID.randomUUID().toString();
  }
}
