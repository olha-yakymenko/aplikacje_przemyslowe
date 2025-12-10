package com.techcorp.employee.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TechCorpEmailValidator implements ConstraintValidator<TechCorpEmail, String> {

    private static final String REQUIRED_DOMAIN = "@techcorp.com";

    @Override
    public void initialize(TechCorpEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        String normalizedEmail = email.toLowerCase().trim();
        return normalizedEmail.endsWith(REQUIRED_DOMAIN.toLowerCase());
    }
}