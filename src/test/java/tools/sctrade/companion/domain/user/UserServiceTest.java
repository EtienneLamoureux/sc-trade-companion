package tools.sctrade.companion.domain.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.sctrade.companion.domain.setting.SettingRepository;
import tools.sctrade.companion.domain.user.idgenerators.RandomUserIdGenerator;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private SettingRepository settings;
  private UserIdGenerator userIdGenerator;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userIdGenerator = new RandomUserIdGenerator();

    this.userService = new UserService(settings, userIdGenerator);
  }

  @Test
  void whenGettingUserThenGetMandatoryFields() {
    var user = userService.get();

    assertNotNull(user.id());
  }
}
