package com.roam.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation that validates a string is a safe title.
 * <p>
 * This annotation ensures that title strings do not contain control characters
 * and are within acceptable length bounds. It is validated by
 * {@link SafeTitleValidator}.
 * The maximum length can be configured via the {@code max} attribute (default:
 * 255).
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * {@literal @}SafeTitle(max = 100)
 * private String title;
 * </pre>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see SafeTitleValidator
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeTitleValidator.class)
@Documented
public @interface SafeTitle {

    String message() default "Title contains invalid characters or exceeds maximum length";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int max() default 255;
}
