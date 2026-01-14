package com.miguelrodriguez19.safecube.user.application.port.out;

import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import java.util.Optional;
import java.util.UUID;

/**
 * UserProfileRepository
 *
 * <p>Outbound port that abstracts persistence operations for {@link UserProfile}.
 */
public interface UserProfileRepository {

  /**
   * Checks whether a user profile already exists for the given account identifier.
   *
   * <p>Used to enforce the one-to-one relationship between account and user profile.
   *
   * @param accountId the account identifier
   * @return {@code true} if a profile exists, {@code false} otherwise
   */
  default boolean existsByAccountId(final UUID accountId) {
    return findByAccountId(accountId).isPresent();
  }

  /**
   * Retrieves a user profile by its associated account identifier.
   *
   * <p>If no profile exists, an empty {@link Optional} is returned.
   *
   * @param accountId the account identifier
   * @return an optional {@link UserProfile}
   */
  Optional<UserProfile> findByAccountId(final UUID accountId);

  /**
   * Persists a new user profile.
   *
   * <p>This operation is expected to be used only when creating new profiles.
   *
   * @param profile the user profile to persist
   */
  void save(final UserProfile profile);
}
