package com.miguelrodriguez19.safecube.auth.application.port.out;

/**
 * PasswordHasher
 *
 * <p>Outbound port responsible for transforming and verifying user passwords.
 */
public interface PasswordHasher {

  /**
   * Hashes a raw password into a secure representation suitable for persistence.
   *
   * @param rawPassword the raw password provided by the user
   * @return the hashed password
   */
  String hash(final String rawPassword);

  /**
   * Verifies a raw password against a previously generated hash.
   *
   * @param rawPassword the raw password provided by the user
   * @param passwordHash the stored password hash
   * @return {@code true} if the password matches, {@code false} otherwise
   */
  boolean matches(final String rawPassword, final String passwordHash);
}
