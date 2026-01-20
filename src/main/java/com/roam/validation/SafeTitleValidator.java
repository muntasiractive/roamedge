package com.roam.validation;

import com.roam.util.InputSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for the {@link SafeTitle} annotation.
 * <p>
 * This validator uses {@link com.roam.util.InputSanitizer} to sanitize title
 * strings
 * and verify they do not exceed the configured maximum length. Null values are
 * considered valid (use {@code @NotNull} for null checks).
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see SafeTitle
 * @see com.roam.util.InputSanitizer#sanitizeTitle(String)
 */
public class SafeTitleValidator implements ConstraintValidator<SafeTitle, String> {

    private int maxLength;

    @Override
    public void initialize(SafeTitle constraintAnnotation) {
        this.maxLength = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }

        try {
            String sanitized = InputSanitizer.sanitizeTitle(value);
            return sanitized.length() <= maxLength;
        } catch (IllegalArgumentException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addConstraintViolation();
            return false;
        }
    }
}
