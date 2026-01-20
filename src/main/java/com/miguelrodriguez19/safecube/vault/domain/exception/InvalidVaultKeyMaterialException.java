package com.miguelrodriguez19.safecube.vault.domain.exception;

import com.miguelrodriguez19.safecube.shared.exception.DomainException;

public class InvalidVaultKeyMaterialException extends DomainException {

  public InvalidVaultKeyMaterialException(final String message) {
    super(message);
  }
}
