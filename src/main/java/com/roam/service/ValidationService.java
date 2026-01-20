package com.roam.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralized validation service using Jakarta Bean Validation (JSR-380).
 * <p>
 * This singleton service provides entity validation before persistence
 * operations,
 * ensuring data integrity and constraint compliance across the application.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li><b>Jakarta Bean Validation:</b> Uses standard JSR-380 annotations for
 * validation</li>
 * <li><b>Multiple Validation Modes:</b> Supports throwing exceptions, returning
 * violations,
 * or simple boolean checks</li>
 * <li><b>Property-level Validation:</b> Can validate individual properties for
 * partial updates</li>
 * <li><b>Custom Exception:</b> Provides {@link ValidationException} for
 * structured error handling</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>{@code
 * ValidationService validator = ValidationService.getInstance();
 * 
 * // Validate and throw exception if invalid
 * validator.validate(entity);
 * 
 * // Check validity without exception
 * if (validator.isValid(entity)) {
 *     // proceed with save
 * }
 * 
 * // Get violations for custom handling
 * Set<ConstraintViolation<MyEntity>> violations = validator.validateAndGetViolations(entity);
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see jakarta.validation.Validator
 * @see ValidationException
 */
public class ValidationService {

    // ==================== Singleton Instance ====================

    private static ValidationService instance;

    // ==================== Instance Fields ====================

    /** The Jakarta Bean Validation validator instance. */
    private final Validator validator;

    // ==================== Constructor ====================

    /**
     * Private constructor to enforce singleton pattern.
     * <p>
     * Initializes the Jakarta Bean Validation factory and creates a validator
     * instance.
     * </p>
     */
    private ValidationService() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    // ==================== Singleton Access ====================

    /**
     * Returns the singleton instance of the ValidationService.
     * <p>
     * This method is thread-safe and uses lazy initialization.
     * </p>
     *
     * @return the singleton ValidationService instance
     */
    public static synchronized ValidationService getInstance() {
        if (instance == null) {
            instance = new ValidationService();
        }
        return instance;
    }

    // ==================== Validation Operations ====================

    /**
     * Validates an entity and throws an exception if validation fails.
     * <p>
     * All constraint violations are collected and formatted into a single
     * exception message with property paths and error descriptions.
     * </p>
     * 
     * @param entity the entity to validate
     * @param <T>    the type of entity being validated
     * @throws ValidationException if one or more validation constraints are
     *                             violated
     */
    public <T> void validate(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);

        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));

            throw new ValidationException("Validation failed: " + errors);
        }
    }

    /**
     * Validates an entity and returns the constraint violations without throwing an
     * exception.
     * <p>
     * This method is useful when you need to handle validation errors in a custom
     * way
     * or aggregate violations from multiple entities.
     * </p>
     * 
     * @param entity the entity to validate
     * @param <T>    the type of entity being validated
     * @return a set of constraint violations, empty if the entity is valid
     */
    public <T> Set<ConstraintViolation<T>> validateAndGetViolations(T entity) {
        return validator.validate(entity);
    }

    /**
     * Checks if an entity passes all validation constraints.
     * <p>
     * This is a convenience method for simple validity checks without
     * needing detailed violation information.
     * </p>
     * 
     * @param entity the entity to check
     * @param <T>    the type of entity being validated
     * @return {@code true} if the entity is valid, {@code false} otherwise
     */
    public <T> boolean isValid(T entity) {
        return validator.validate(entity).isEmpty();
    }

    /**
     * Validates a specific property of an entity.
     * <p>
     * This method is useful for validating individual fields, such as during
     * partial updates or real-time form validation.
     * </p>
     * 
     * @param entity       the entity containing the property
     * @param propertyName the name of the property to validate
     * @param <T>          the type of entity being validated
     * @throws ValidationException if the property validation fails
     */
    public <T> void validateProperty(T entity, String propertyName) {
        Set<ConstraintViolation<T>> violations = validator.validateProperty(entity, propertyName);

        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));

            throw new ValidationException("Property '" + propertyName + "' validation failed: " + errors);
        }
    }

    // ==================== Inner Classes ====================

    /**
     * Custom runtime exception thrown when validation constraints are violated.
     * <p>
     * This exception provides a structured way to communicate validation failures
     * throughout the application without requiring checked exception handling.
     * </p>
     * 
     * @author Roam Development Team
     * @version 1.0
     * @since 1.0
     */
    public static class ValidationException extends RuntimeException {

        /**
         * Constructs a new ValidationException with the specified error message.
         *
         * @param message the detail message describing the validation failure
         */
        public ValidationException(String message) {
            super(message);
        }

        /**
         * Constructs a new ValidationException with the specified error message and
         * cause.
         *
         * @param message the detail message describing the validation failure
         * @param cause   the underlying cause of the validation failure
         */
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
