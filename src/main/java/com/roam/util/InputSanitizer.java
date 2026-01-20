package com.roam.util;

import org.owasp.encoder.Encode;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing and validating user inputs to prevent injection
 * attacks.
 * 
 * <p>
 * This class provides comprehensive input sanitization methods to protect
 * against
 * various security vulnerabilities including SQL/JPQL injection, Lucene query
 * injection,
 * and cross-site scripting (XSS) attacks.
 * </p>
 * 
 * <h2>Security Features</h2>
 * <ul>
 * <li>JPQL/SQL injection prevention - escapes and validates database query
 * inputs</li>
 * <li>Lucene query injection prevention - sanitizes search query special
 * characters</li>
 * <li>XSS prevention - encodes HTML and JavaScript content for safe
 * display</li>
 * <li>Query complexity validation - prevents DoS through excessive
 * wildcards</li>
 * <li>Length validation - enforces maximum input lengths for all field
 * types</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Sanitize search input
 * String safeQuery = InputSanitizer.sanitizeSearchQuery(userInput);
 * 
 * // Sanitize HTML content
 * String safeHtml = InputSanitizer.sanitizeHtml(htmlContent);
 * 
 * // Validate and sanitize title
 * String safeTitle = InputSanitizer.sanitizeTitle(title);
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class InputSanitizer {

    // ========================================
    // CONSTANTS - Maximum Lengths
    // ========================================

    // Maximum query lengths
    public static final int MAX_SEARCH_QUERY_LENGTH = 500;
    public static final int MAX_TITLE_LENGTH = 255;
    public static final int MAX_CONTENT_LENGTH = 100000; // 100KB
    public static final int MAX_DESCRIPTION_LENGTH = 5000;

    // Patterns for detecting malicious input
    /** Pattern to detect SQL/JPQL injection attempts. */
    private static final Pattern JPQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(SELECT|UPDATE|DELETE|INSERT|DROP|CREATE|ALTER|EXEC|EXECUTE|UNION|--|\\/\\*|\\*\\/|;)",
            Pattern.CASE_INSENSITIVE);

    /** Pattern to identify Lucene special characters that need escaping. */
    private static final Pattern LUCENE_SPECIAL_CHARS = Pattern.compile(
            "[+\\-!(){}\\[\\]^\"~*?:\\\\/]");

    /** Pattern to detect excessive wildcards (potential DoS attack). */
    private static final Pattern EXCESSIVE_WILDCARDS = Pattern.compile(
            "([*?].*){5,}");

    // ========================================
    // CONSTRUCTOR
    // ========================================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private InputSanitizer() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // SEARCH QUERY SANITIZATION
    // ========================================

    /**
     * Sanitizes a search query for use with Lucene.
     * Escapes special characters and validates query complexity.
     * 
     * @param query The raw search query
     * @return Sanitized query safe for Lucene
     * @throws IllegalArgumentException if query is too long or too complex
     */
    public static String sanitizeSearchQuery(String query) {
        if (query == null) {
            return "";
        }

        query = query.trim();

        // Length validation
        if (query.length() > MAX_SEARCH_QUERY_LENGTH) {
            throw new IllegalArgumentException(
                    "Search query too long. Maximum length is " + MAX_SEARCH_QUERY_LENGTH + " characters.");
        }

        // Check for excessive wildcards (DoS prevention)
        if (EXCESSIVE_WILDCARDS.matcher(query).find()) {
            throw new IllegalArgumentException(
                    "Search query too complex. Please reduce the number of wildcards.");
        }

        // Escape Lucene special characters
        // Allow wildcards (*?) but escape other special chars
        String sanitized = LUCENE_SPECIAL_CHARS.matcher(query).replaceAll("\\\\$0");

        // Remove any potential JPQL injection attempts
        if (JPQL_INJECTION_PATTERN.matcher(sanitized).find()) {
            throw new IllegalArgumentException(
                    "Invalid search query. Please remove SQL keywords.");
        }

        return sanitized;
    }

    // ========================================
    // DATABASE QUERY SANITIZATION
    // ========================================

    /**
     * Sanitizes text for JPQL queries.
     * Prevents SQL/JPQL injection attacks.
     * 
     * @param input The raw input text
     * @return Sanitized text safe for JPQL queries
     * @throws IllegalArgumentException if SQL injection patterns are detected
     */
    public static String sanitizeForJPQL(String input) {
        if (input == null) {
            return "";
        }

        input = input.trim();

        // Check for SQL injection patterns
        if (JPQL_INJECTION_PATTERN.matcher(input).find()) {
            throw new IllegalArgumentException(
                    "Invalid input detected. Please remove special SQL characters.");
        }

        // Escape single quotes for JPQL string literals
        return input.replace("'", "''");
    }

    // ========================================
    // XSS PREVENTION
    // ========================================

    /**
     * Sanitizes HTML content to prevent XSS attacks.
     * Encodes special HTML characters.
     * 
     * @param html The raw HTML content
     * @return HTML-encoded safe content
     */
    public static String sanitizeHtml(String html) {
        if (html == null) {
            return "";
        }
        return Encode.forHtml(html);
    }

    /**
     * Sanitizes JavaScript content to prevent XSS attacks.
     * 
     * @param js The raw JavaScript content
     * @return JavaScript-encoded safe content
     */
    public static String sanitizeJavaScript(String js) {
        if (js == null) {
            return "";
        }
        return Encode.forJavaScript(js);
    }

    // ========================================
    // FIELD SANITIZATION
    // ========================================

    /**
     * Validates and sanitizes a title field.
     * 
     * @param title The raw title
     * @return Sanitized title
     * @throws IllegalArgumentException if title is too long
     */
    public static String sanitizeTitle(String title) {
        if (title == null) {
            return "";
        }

        title = title.trim();

        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Title too long. Maximum length is " + MAX_TITLE_LENGTH + " characters.");
        }

        // Remove control characters
        return title.replaceAll("\\p{Cntrl}", "");
    }

    /**
     * Validates and sanitizes a description field.
     * 
     * @param description The raw description
     * @return Sanitized description
     * @throws IllegalArgumentException if description is too long
     */
    public static String sanitizeDescription(String description) {
        if (description == null) {
            return "";
        }

        description = description.trim();

        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                    "Description too long. Maximum length is " + MAX_DESCRIPTION_LENGTH + " characters.");
        }

        // Remove control characters except newlines and tabs
        return description.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "");
    }

    /**
     * Validates and sanitizes content field (for wikis, notes, etc.).
     * 
     * @param content The raw content
     * @return Sanitized content
     * @throws IllegalArgumentException if content is too long
     */
    public static String sanitizeContent(String content) {
        if (content == null) {
            return "";
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException(
                    "Content too long. Maximum length is " + MAX_CONTENT_LENGTH + " characters.");
        }

        // Allow newlines and tabs in content
        return content.replaceAll("[\\p{Cntrl}&&[^\n\t\r]]", "");
    }

    // ========================================
    // VALIDATION METHODS
    // ========================================

    /**
     * Validates a string is not null and not empty.
     * 
     * @param value     The value to validate
     * @param fieldName The name of the field for error messages
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }

    /**
     * Validates a string length is within bounds.
     * 
     * @param value     The value to validate
     * @param fieldName The name of the field for error messages
     * @param minLength Minimum length (inclusive)
     * @param maxLength Maximum length (inclusive)
     * @throws IllegalArgumentException if length is out of bounds
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null.");
        }

        int length = value.length();
        if (length < minLength || length > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + minLength + " and " + maxLength + " characters. Current: "
                            + length);
        }
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Strips all HTML tags from a string.
     * Useful for preview snippets.
     * 
     * @param html The HTML content
     * @return Plain text without HTML tags
     */
    public static String stripHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "");
    }

    /**
     * Truncates a string to a maximum length, adding ellipsis if truncated.
     * 
     * @param text      The text to truncate
     * @param maxLength Maximum length
     * @return Truncated text
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
