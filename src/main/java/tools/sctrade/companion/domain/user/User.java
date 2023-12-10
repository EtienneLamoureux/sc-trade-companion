package tools.sctrade.companion.domain.user;

public record User(String id, String label) {
  public User withLabel(String label) {
    return new User(id(), label);
  }
}
