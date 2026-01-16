package unit.com.miguelrodriguez19.safecube.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.auth.application.port.out.AuthAccountRepository;
import com.miguelrodriguez19.safecube.user.infrastructure.persistence.AccountExistenceAuthAdapter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class AccountExistenceAuthAdapterTest {

  @Mock private AuthAccountRepository authAccountRepository;

  @InjectMocks private AccountExistenceAuthAdapter target;

  @Test
  void shouldReturnTrue_whenAccountExists() {
    final var accountId = UUID.randomUUID();

    when(authAccountRepository.existsByAccountId(accountId)).thenReturn(true);

    final var result = target.existsByAccountId(accountId);

    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalse_whenAccountDoesNotExist() {
    final var accountId = UUID.randomUUID();

    when(authAccountRepository.existsByAccountId(accountId)).thenReturn(false);

    final var result = target.existsByAccountId(accountId);

    assertThat(result).isFalse();
  }
}
