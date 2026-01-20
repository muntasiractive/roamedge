package com.roam.validation;

import com.roam.util.InputSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for the {@link SafeSearchQuery} annotation.
 * <p>
 * This validator uses {@link com.roam.util.InputSanitizer} to verify that
 * search
 * queries do not contain malicious content or injection attempts. Empty or null
 * queries are considered valid (they will simply return no results).
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see SafeSearchQuery
 * @see com.roam.util.InputSanitizer#sanitizeSearchQuery(String)
 */
public class SafeSearchQueryValidator implements ConstraintValidator<SafeSearchQuery, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Empty queries are valid (will return no results)
        }

        try {
            InputSanitizer.sanitizeSearchQuery(value);
            return true;
        } catch (IllegalArgumentException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addConstraintViolation();
            return false;
        }
    }
}
