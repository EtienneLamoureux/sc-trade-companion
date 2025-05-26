package tools.sctrade.companion.domain.user;

import tools.sctrade.companion.utils.HashUtil;

/**
 * Generates a unique user id. As users do not authenticate, we construct a unique identifier from
 * the machine's attributes the program is being run on.
 */
public abstract class UserIdGenerator {
  String getId() {
    return HashUtil.hash(generateId());
  }

  /**
   * Generates a unique id for this computer or session.
   *
   * @return A String that uniquely identifies this computer or session
   */
  protected abstract String generateId();
}
