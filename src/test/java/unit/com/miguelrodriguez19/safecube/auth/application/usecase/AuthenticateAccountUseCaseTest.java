package unit.com.miguelrodriguez19.safecube.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.miguelrodriguez19.safecube.auth.application.dto.AuthenticateAccountCommand;
import com.miguelrodriguez19.safecube.auth.application.error.AuthError;
import com.miguelrodriguez19.safecube.auth.application.port.out.AuthAccountRepository;
import com.miguelrodriguez19.safecube.auth.application.port.out.PasswordHasher;
import com.miguelrodriguez19.safecube.auth.application.usecase.AuthenticateAccountUseCase;
import com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount;
import com.miguelrodriguez19.safecube.shared.result.Result;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class AuthenticateAccountUseCaseTest {

  @Mock private AuthAccountRepository repository;
  @Mock private PasswordHasher passwordHasher;

  @InjectMocks private AuthenticateAccountUseCase target;

  @Test
  void shouldAuthenticateAccountSuccessfully() {
    final var command = getAuthenticateAccountCommand();
    final var account = enabledAccount(command.email());

    when(repository.findByEmail(command.email())).thenReturn(Optional.of(account));
    when(passwordHasher.matches(command.rawPassword(), account.getPasswordHash())).thenReturn(true);

    final var result = target.execute(command);

    assertThat(result).isInstanceOf(Result.Success.class);
    assertThat(result.success()).isPresent();
    assertThat(result.success().get().accountId()).isEqualTo(account.getAccountId());
  }

  @Test
  void shouldFail_whenAccountDoesNotExist() {
    final var command = getAuthenticateAccountCommand();

    when(repository.findByEmail(command.email())).thenReturn(Optional.empty());

    final var result = target.execute(command);

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error()).containsInstanceOf(AuthError.AccountNotFound.class);

    verifyNoInteractions(passwordHasher);
  }

  @Test
  void shouldFail_whenPasswordIsInvalid() {
    final var command = getAuthenticateAccountCommand();
    final var account = enabledAccount(command.email());

    when(repository.findByEmail(command.email())).thenReturn(Optional.of(account));
    when(passwordHasher.matches(command.rawPassword(), account.getPasswordHash()))
        .thenReturn(false);

    final var result = target.execute(command);

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error()).containsInstanceOf(AuthError.InvalidCredentials.class);
  }

  @Test
  void shouldFail_whenAccountIsDisabled() {
    final var command = getAuthenticateAccountCommand();
    final var account = disabledAccount(command.email());

    when(repository.findByEmail(command.email())).thenReturn(Optional.of(account));

    final var result = target.execute(command);

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error()).containsInstanceOf(AuthError.AccountDisabled.class);

    verifyNoInteractions(passwordHasher);
  }

  private AuthenticateAccountCommand getAuthenticateAccountCommand() {
    return new AuthenticateAccountCommand("test@safecube.io", "password123");
  }

  private AuthAccount enabledAccount(final String email) {
    return AuthAccount.of(email, "hashedPassword", Instant.now());
  }

  private AuthAccount disabledAccount(final String email) {
    final var account =
        AuthAccount.of(email, "hashedPassword", Instant.now().minus(3, ChronoUnit.DAYS));
    account.disable(Instant.now());
    return account;
  }
}
