package com.miguelrodriguez19.safecube.auth.application.usecase;

import com.miguelrodriguez19.safecube.auth.application.dto.RegisterAccountCommand;
import com.miguelrodriguez19.safecube.auth.application.dto.RegisterAccountResult;
import com.miguelrodriguez19.safecube.auth.application.error.AuthError;
import com.miguelrodriguez19.safecube.auth.application.port.out.AuthAccountRepository;
import com.miguelrodriguez19.safecube.auth.application.port.out.PasswordHasher;
import com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount;
import com.miguelrodriguez19.safecube.shared.result.Result;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

/**
 * RegisterAccountUseCase
 *
 * <p>Registers a new authentication account using provided credentials.
 *
 * <p>This use case enforces uniqueness of email and applies the minimum credential validation rules
 * defined for the system.
 *
 * @see "docs/use-case/auth_use_cases_safe_cube_backend_v_1"
 */
@RequiredArgsConstructor
public class RegisterAccountUseCase {

  private final AuthAccountRepository repository;
  private final PasswordHasher passwordHasher;

  public Result<RegisterAccountResult, AuthError> execute(final RegisterAccountCommand command) {
    final var email = command.email();

    if (repository.existsByEmail(email)) {
      return Result.failure(new AuthError.AccountAlreadyExists());
    }

    final var passwordHash = passwordHasher.hash(command.rawPassword());
    final var now = Instant.now();

    final var account = AuthAccount.of(email, passwordHash, now);
    repository.save(account);

    return Result.success(
        new RegisterAccountResult(account.getAccountId(), account.getCreatedAt()));
  }
}
