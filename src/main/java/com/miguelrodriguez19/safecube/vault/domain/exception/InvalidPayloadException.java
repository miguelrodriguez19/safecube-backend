package com.miguelrodriguez19.safecube.vault.domain.exception;

import com.miguelrodriguez19.safecube.shared.exception.DomainException;

public final class InvalidPayloadException extends DomainException {

  public InvalidPayloadException(final String message) {
    super(message);
  }
}
