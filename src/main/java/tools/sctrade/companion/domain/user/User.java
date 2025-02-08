package tools.sctrade.companion.domain.user;

/**
 * Represents a user of this app.
 */
public record User(String id, String label) {
  /**
   * Builds a copy of this user with a new label.
   *
   * @param label New label.
   * @return New user instance with the specified label.
   */
  public User withLabel(String label) {
    return new User(id(), label);
  }
}
