package com.miguelrodriguez19.safecube.vault.application.error;

/** Base type for vault application errors. */
public sealed interface VaultError
    permits VaultError.InvalidPayload,
        VaultError.SecureItemNotFound,
        VaultError.StaleUpdateRejected,
        VaultError.StaleDeleteRejected,
        VaultError.IdempotencyConflict {

  /** Indicates that the provided payload is invalid. */
  record InvalidPayload() implements VaultError {}

  /** Indicates that a SecureItem was not found for the given account. */
  record SecureItemNotFound() implements VaultError {}

  /** Indicates that an update was rejected due to stale data. */
  record StaleUpdateRejected() implements VaultError {}

  /** Indicates that a delete operation was rejected due to stale data. */
  record StaleDeleteRejected() implements VaultError {}

  /** Indicates that an idempotency key was reused for a different request. */
  record IdempotencyConflict() implements VaultError {}
}
