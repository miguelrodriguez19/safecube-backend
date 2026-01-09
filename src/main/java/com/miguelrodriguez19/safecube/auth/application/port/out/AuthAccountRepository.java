package com.miguelrodriguez19.safecube.auth.application.port.out;

import com.miguelrodriguez19.safecube.auth.domain.model.AuthAccount;
import java.util.Optional;

/**
 * AuthAccountRepository
 *
 * <p>Outbound port that abstracts persistence operations for {@link AuthAccount}.
 */
public interface AuthAccountRepository {

  /**
   * Checks whether an account already exists for the given email.
   *
   * <p>Used to enforce uniqueness during account registration.
   *
   * @param email the email to check
   * @return {@code true} if an account exists, {@code false} otherwise
   */
  boolean existsByEmail(final String email);

  /**
   * Retrieves an account by its email identifier.
   *
   * <p>If no account exists, an empty {@link Optional} is returned.
   *
   * @param email the email identifier
   * @return an optional {@link AuthAccount}
   */
  Optional<AuthAccount> findByEmail(final String email);

  /**
   * Persists a new authentication account.
   *
   * <p>This operation is expected to be used only when creating new accounts.
   *
   * @param account the account to persist
   */
  void save(final AuthAccount account);
}
