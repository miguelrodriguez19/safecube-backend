package com.miguelrodriguez19.safecube.vault.domain.exception;

import com.miguelrodriguez19.safecube.shared.exception.DomainException;

public class InvalidWrappedKekException extends DomainException {

  public InvalidWrappedKekException(final String message) {
    super(message);
  }
}
