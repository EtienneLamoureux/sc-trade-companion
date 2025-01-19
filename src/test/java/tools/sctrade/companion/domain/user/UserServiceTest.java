package tools.sctrade.companion.domain.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.setting.SettingRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private SettingRepository settings;

  private UserService userService;

  @BeforeEach
  public void setUp() {
    this.userService = new UserService(settings);
  }

  @Test
  void whenGettingUserThenGetMandatoryFields() {
    var user = userService.get();

    assertNotNull(user.id());
  }
}
