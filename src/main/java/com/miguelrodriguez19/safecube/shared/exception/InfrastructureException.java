package com.miguelrodriguez19.safecube.shared.exception;

/**
 * InfrastructureException
 *
 * <p>Base exception for infrastructure-level failures.
 *
 * <p>Represents non-recoverable technical errors such as:
 *
 * <ul>
 *   <li>Cryptography misconfiguration
 *   <li>Database connectivity issues
 *   <li>Filesystem or network failures
 * </ul>
 *
 * <p>These errors indicate a faulty runtime environment and should generally cause the application
 * to fail fast.
 */
public abstract class InfrastructureException extends RuntimeException {

  protected InfrastructureException(final String message) {
    super(message);
  }

  protected InfrastructureException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
