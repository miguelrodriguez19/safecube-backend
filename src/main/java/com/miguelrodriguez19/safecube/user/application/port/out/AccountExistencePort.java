package com.miguelrodriguez19.safecube.user.application.port.out;

import java.util.UUID;

/**
 * AccountExistencePort
 *
 * <p>Outbound port used by the user slice to verify the existence of an authentication account.
 *
 * <p>This port allows the user domain to remain decoupled from the auth slice implementation.
 */
public interface AccountExistencePort {

  /**
   * Checks whether an authentication account exists for the given account identifier.
   *
   * @param accountId the account identifier
   * @return {@code true} if the account exists, {@code false} otherwise
   */
  boolean existsByAccountId(final UUID accountId);
}
