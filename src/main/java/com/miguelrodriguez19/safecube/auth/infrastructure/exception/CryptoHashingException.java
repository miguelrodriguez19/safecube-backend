package com.miguelrodriguez19.safecube.auth.infrastructure.exception;

import com.miguelrodriguez19.safecube.shared.exception.InfrastructureException;

/**
 * CryptoHashingException
 *
 * <p>Infrastructure-level exception thrown when a cryptographic hashing operation cannot be
 * performed due to misconfiguration or invalid runtime state.
 *
 * <p>This exception represents a non-recoverable error and should cause the application to fail
 * fast.
 */
public class CryptoHashingException extends InfrastructureException {

  public CryptoHashingException(final String message, final Exception cause) {
    super(message, cause);
  }
}
