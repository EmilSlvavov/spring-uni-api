package org.chud.springuniapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotAdminValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotAdmin {

    String message() default "an admin cannot be enrolled in courses";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
