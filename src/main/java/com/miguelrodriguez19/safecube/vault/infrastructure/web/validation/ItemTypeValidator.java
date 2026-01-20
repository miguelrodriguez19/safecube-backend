package com.miguelrodriguez19.safecube.vault.infrastructure.web.validation;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.ItemTypeDto;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.annotation.ValidItemType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ItemTypeValidator implements ConstraintValidator<ValidItemType, String> {

  @Override
  public boolean isValid(final String value, final ConstraintValidatorContext context) {

    if (value == null) {
      return true;
    }

    try {
      ItemTypeDto.valueOf(value);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
