package com.miguelrodriguez19.safecube.vault.application.error;

/**
 * VaultKeyMaterialError
 *
 * <p>Base type for all expected vault key material application errors.
 */
public sealed interface VaultKeyMaterialError
    permits VaultKeyMaterialError.VaultAlreadyInitialized,
        VaultKeyMaterialError.VaultNotInitialized,
        VaultKeyMaterialError.StaleMasterWrappedKekUpdate {

  /** Returned when attempting to initialize a vault that already exists. */
  record VaultAlreadyInitialized() implements VaultKeyMaterialError {}

  /** Returned when attempting to access or modify a vault that does not exist. */
  record VaultNotInitialized() implements VaultKeyMaterialError {}

  /** Returned when a master-wrapped KEK update uses an obsolete revision. */
  record StaleMasterWrappedKekUpdate() implements VaultKeyMaterialError {}
}
