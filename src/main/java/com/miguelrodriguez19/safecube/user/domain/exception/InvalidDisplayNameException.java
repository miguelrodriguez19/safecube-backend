package com.miguelrodriguez19.safecube.user.domain.exception;

import com.miguelrodriguez19.safecube.shared.exception.DomainException;

public final class InvalidDisplayNameException extends DomainException {

  public InvalidDisplayNameException() {
    super("DisplayName exceeds max length");
  }
}
