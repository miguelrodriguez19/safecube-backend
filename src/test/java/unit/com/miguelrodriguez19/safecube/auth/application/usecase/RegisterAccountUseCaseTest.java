package unit.com.miguelrodriguez19.safecube.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.miguelrodriguez19.safecube.auth.application.dto.RegisterAccountCommand;
import com.miguelrodriguez19.safecube.auth.application.error.AuthError;
import com.miguelrodriguez19.safecube.auth.application.port.out.AuthAccountRepository;
import com.miguelrodriguez19.safecube.auth.application.port.out.PasswordHasher;
import com.miguelrodriguez19.safecube.auth.application.usecase.RegisterAccountUseCase;
import com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount;
import com.miguelrodriguez19.safecube.shared.result.Result;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import unit.annotation.UnitTest;

@UnitTest
class RegisterAccountUseCaseTest {

  @Mock private AuthAccountRepository repository;
  @Mock private PasswordHasher passwordHasher;

  @InjectMocks private RegisterAccountUseCase target;

  @Test
  void shouldRegisterAccountSuccessfully() {
    final var command = getRegisterAccountCommand();

    when(repository.existsByEmail(command.email())).thenReturn(false);

    final var hashedPassword = "hashedPassword";
    when(passwordHasher.hash(command.rawPassword())).thenReturn(hashedPassword);

    when(repository.save(any(AuthAccount.class))).thenReturn(1);

    final var result = target.execute(command);

    assertThat(result).isInstanceOf(Result.Success.class);
    assertThat(result.success()).isPresent();
    assertThat(result.success().get().accountId()).isNotNull();
  }

  @Test
  void shouldFail_whenEmailAlreadyExists() {
    final var command = getRegisterAccountCommand();

    when(repository.existsByEmail(command.email())).thenReturn(true);

    final var result = target.execute(command);

    assertThat(result).isInstanceOf(Result.Failure.class);
    assertThat(result.error()).containsInstanceOf(AuthError.AccountAlreadyExists.class);

    verifyNoInteractions(passwordHasher);
    verify(repository, never()).save(any(AuthAccount.class));
  }

  private RegisterAccountCommand getRegisterAccountCommand() {
    return new RegisterAccountCommand("test@safecube.io", "password123");
  }
}
