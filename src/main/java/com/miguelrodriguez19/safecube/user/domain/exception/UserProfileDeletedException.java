package com.miguelrodriguez19.safecube.user.domain.exception;

import com.miguelrodriguez19.safecube.shared.exception.DomainException;

public final class UserProfileDeletedException extends DomainException {

  public UserProfileDeletedException() {
    super("UserProfile is deleted and cannot be modified");
  }
}
