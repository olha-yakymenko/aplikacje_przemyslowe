package com.techcorp.employee.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TechCorpEmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TechCorpEmail {
    String message() default "Email musi być w domenie @techcorp.com";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}