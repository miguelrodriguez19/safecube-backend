package com.miguelrodriguez19.safecube.user.infrastructure.persistence;

import com.miguelrodriguez19.safecube.auth.application.port.out.AuthAccountRepository;
import com.miguelrodriguez19.safecube.user.application.port.out.AccountExistencePort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AccountExistenceAuthAdapter
 *
 * <p>Infrastructure adapter that verifies the existence of authentication accounts by delegating to
 * the auth slice.
 *
 * <p>This adapter acts as an anti-corruption layer between the user and auth slices.
 */
@Component
@RequiredArgsConstructor
public class AccountExistenceAuthAdapter implements AccountExistencePort {

  private final AuthAccountRepository authAccountRepository;

  /**
   * Checks whether an authentication account exists for the given account identifier.
   *
   * <p>This method does not check account status (enabled/disabled), only existence.
   *
   * @param accountId the account identifier
   * @return {@code true} if the account exists, {@code false} otherwise
   */
  @Override
  public boolean existsByAccountId(final UUID accountId) {
    return authAccountRepository.existsByAccountId(accountId);
  }
}
