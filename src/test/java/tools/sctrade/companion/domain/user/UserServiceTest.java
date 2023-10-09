package tools.sctrade.companion.domain.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {
  private UserService userService;

  @BeforeEach
  public void setUp() {
    this.userService = new UserService();
  }

  @Test
  public void whenGettingUserThenGetMandatoryFields() {
    var user = userService.get();

    assertNotNull(user.id());
  }
}
