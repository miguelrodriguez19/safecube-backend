package com.miguelrodriguez19.safecube.user.application.error;

/**
 * UserError
 *
 * <p>Base type for all expected user-profile-related application errors.
 */
public sealed interface UserError
    permits UserError.UserProfileAlreadyExists,
        UserError.AccountNotFound,
        UserError.UserProfileNotFound,
        UserError.InvalidDisplayName {

  /** Returned when attempting to create a user profile that already exists. */
  record UserProfileAlreadyExists() implements UserError {}

  /** Returned when attempting to create a profile for a non-existing account. */
  record AccountNotFound() implements UserError {}

  /** Returned when attempting to find an unexisting profile */
  record UserProfileNotFound() implements UserError {}

  /** Returned when attempting to validate a UserProfile.displayName */
  record InvalidDisplayName() implements UserError {}
}
