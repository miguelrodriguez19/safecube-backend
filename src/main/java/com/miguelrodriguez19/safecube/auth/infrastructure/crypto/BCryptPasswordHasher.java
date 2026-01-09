package com.miguelrodriguez19.safecube.auth.infrastructure.crypto;

import com.miguelrodriguez19.safecube.auth.application.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCryptPasswordHasher
 *
 * <p>Infrastructure adapter for password hashing using BCrypt.
 */
@Component
public class BCryptPasswordHasher implements PasswordHasher {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public String hash(final String rawPassword) {
    return encoder.encode(rawPassword);
  }

  @Override
  public boolean matches(final String rawPassword, final String passwordHash) {
    return encoder.matches(rawPassword, passwordHash);
  }
}
