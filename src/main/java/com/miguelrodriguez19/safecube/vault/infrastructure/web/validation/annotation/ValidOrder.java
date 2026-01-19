package com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.annotation;

import com.miguelrodriguez19.safecube.vault.infrastructure.web.validation.OrderValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OrderValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOrder {

  String message() default "Invalid itemType";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
