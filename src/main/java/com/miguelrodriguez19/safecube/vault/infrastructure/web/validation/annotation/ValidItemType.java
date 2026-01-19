package com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.annotation;

import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.ItemTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ItemTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidItemType {

  String message() default "Invalid itemType";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
