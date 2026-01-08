package com.miguelrodriguez19.safecube.auth.application.usecase;

import com.miguelrodriguez19.safecube.auth.application.dto.AuthenticateAccountCommand;
import com.miguelrodriguez19.safecube.auth.application.dto.AuthenticateAccountResult;
import com.miguelrodriguez19.safecube.auth.application.error.AuthError;
import com.miguelrodriguez19.safecube.auth.application.port.out.AuthAccountRepository;
import com.miguelrodriguez19.safecube.auth.application.port.out.PasswordHasher;
import com.miguelrodriguez19.safecube.shared.result.Result;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

/**
 * AuthenticateAccountUseCase
 *
 * <p>Authenticates an existing account by validating provided credentials.
 *
 * @see "docs/use-case/auth_use_cases_safe_cube_backend_v_1"
 */
@RequiredArgsConstructor
public class AuthenticateAccountUseCase {

  private final AuthAccountRepository repository;
  private final PasswordHasher passwordHasher;

  public Result<AuthenticateAccountResult, AuthError> execute(
      final AuthenticateAccountCommand command) {

    final var accountOpt = repository.findByEmail(command.email());
    if (accountOpt.isEmpty()) {
      return Result.failure(new AuthError.AccountNotFound());
    }

    final var account = accountOpt.get();

    if (!account.isEnabled()) {
      return Result.failure(new AuthError.AccountDisabled());
    }

    final var matches = passwordHasher.matches(command.rawPassword(), account.getPasswordHash());
    if (!matches) {
      return Result.failure(new AuthError.InvalidCredentials());
    }

    return Result.success(new AuthenticateAccountResult(account.getAccountId(), Instant.now()));
  }
}
