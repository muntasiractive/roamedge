package com.roam.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation that validates a string is a safe search query.
 * <p>
 * This annotation ensures that search queries do not contain injection attempts
 * and maintain reasonable complexity. It is validated by
 * {@link SafeSearchQueryValidator}.
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * public void search(@SafeSearchQuery String query) {
 *     // query is guaranteed to be safe
 * }
 * </pre>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see SafeSearchQueryValidator
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeSearchQueryValidator.class)
@Documented
public @interface SafeSearchQuery {

    String message() default "Search query contains invalid characters or is too complex";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
