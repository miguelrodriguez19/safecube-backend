package com.miguelrodriguez19.safecube.vault.infrastructure.web.validation;

import com.miguelrodriguez19.safecube.vault.application.dto.secureitem.query.ListSecureItemsFilter.Order;
import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.annotation.ValidOrder;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OrderValidator implements ConstraintValidator<ValidOrder, String> {

  @Override
  public boolean isValid(final String value, final ConstraintValidatorContext context) {

    if (value == null) {
      return true;
    }

    try {
      Order.valueOf(value);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
