package com.miguelrodriguez19.safecube.shared.exception;

/**
 * DomainException
 *
 * <p>Base exception for domain-level rule violations.
 *
 * <p>Represents errors caused by invalid domain states or broken business invariants. These
 * exceptions indicate that an operation violates the rules of the domain model.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>Attempting an invalid state transition
 *   <li>Using a value object with invalid constraints
 *   <li>Breaking an aggregate invariant
 * </ul>
 *
 * <p>DomainExceptions are deterministic and should be triggered exclusively by domain logic, never
 * by infrastructure concerns.
 */
public abstract class DomainException extends RuntimeException {

  protected DomainException(final String message) {
    super(message);
  }
}
