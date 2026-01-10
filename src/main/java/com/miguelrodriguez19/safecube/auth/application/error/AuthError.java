package com.miguelrodriguez19.safecube.auth.application.error;

/**
 * AuthError
 *
 * <p>Base type for all expected authentication-related application errors.
 */
public sealed interface AuthError
    permits AuthError.AccountAlreadyExists,
        AuthError.InvalidCredentials,
        AuthError.AccountNotFound,
        AuthError.AccountDisabled {

  /**
   * Returned when attempting to register an account with an email that already exists in the
   * system.
   */
  record AccountAlreadyExists() implements AuthError {}

  /**
   * Returned when provided credentials do not meet minimum requirements or do not match stored
   * credentials.
   *
   * <p>Used both for registration and authentication failures.
   */
  record InvalidCredentials() implements AuthError {}

  /** Returned when attempting to authenticate a non-existing account. */
  record AccountNotFound() implements AuthError {}

  /** Returned when attempting to authenticate a disabled account. */
  record AccountDisabled() implements AuthError {}
}
