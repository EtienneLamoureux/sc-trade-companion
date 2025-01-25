package tools.sctrade.companion.domain.user;

/**
 * Represents a user of this app.
 */
public record User(String id, String label) {
  public User withLabel(String label) {
    return new User(id(), label);
  }
}
