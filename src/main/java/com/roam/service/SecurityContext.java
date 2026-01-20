package com.roam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Security context for managing authentication state and PIN verification.
 * <p>
 * This singleton class provides centralized security management for the Roam
 * application,
 * handling user authentication through PIN verification with industry-standard
 * security practices.
 * </p>
 * 
 * <h2>Security Features:</h2>
 * <ul>
 * <li><b>BCrypt Password Hashing:</b> Uses BCrypt with strength 12 (4096
 * rounds) for secure
 * password hashing with automatic salting</li>
 * <li><b>Rate Limiting:</b> Prevents brute force attacks with a 3-attempt limit
 * before
 * 60-second lockout period</li>
 * <li><b>Minimum PIN Length:</b> Enforces 8-character minimum for PIN
 * security</li>
 * <li><b>Legacy Migration:</b> Supports automatic migration from SHA-256 to
 * BCrypt hashing</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * 
 * <pre>{@code
 * SecurityContext security = SecurityContext.getInstance();
 * if (security.isLockEnabled()) {
 *     boolean success = security.authenticate(userPin);
 *     if (success) {
 *         // User authenticated
 *     }
 * }
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see SettingsService
 * @ThreadSafe This class is thread-safe for concurrent authentication attempts
 */
public class SecurityContext {

    // ==================== Constants ====================

    private static final Logger logger = LoggerFactory.getLogger(SecurityContext.class);

    // ==================== Singleton Instance ====================

    private static SecurityContext instance;

    // ==================== Instance Fields ====================

    private boolean authenticated = false;

    // ==================== Security Configuration ====================

    /**
     * BCrypt encoder with strength 12 (2^12 = 4096 rounds) for secure password
     * hashing.
     */
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /** Minimum PIN length required for security compliance. */
    private static final int MIN_PIN_LENGTH = 8;

    // ==================== Rate Limiting Fields ====================

    /** Counter for failed authentication attempts since last successful login. */
    private int failedAttempts = 0;

    /** Maximum failed attempts allowed before triggering lockout. */
    private static final int MAX_FAILED_ATTEMPTS_BEFORE_DELAY = 3;

    /** Timestamp when lockout started (0 means no lockout is active). */
    private long lockoutStartTime = 0;

    /** Lockout duration in milliseconds (60 seconds). */
    private static final long LOCKOUT_DURATION_MS = 60_000;

    // ==================== Constructor ====================

    /**
     * Private constructor to enforce singleton pattern.
     * Use {@link #getInstance()} to obtain the singleton instance.
     */
    private SecurityContext() {
    }

    // ==================== Singleton Access ====================

    /**
     * Returns the singleton instance of the SecurityContext.
     * <p>
     * This method is thread-safe and uses lazy initialization.
     * </p>
     *
     * @return the singleton SecurityContext instance
     */
    public static synchronized SecurityContext getInstance() {
        if (instance == null) {
            instance = new SecurityContext();
        }
        return instance;
    }

    // ==================== Authentication State ====================

    /**
     * Checks if the current session is authenticated.
     *
     * @return {@code true} if the user has successfully authenticated,
     *         {@code false} otherwise
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Sets the authentication state.
     * <p>
     * When set to {@code true}, the failed attempts counter is reset.
     * </p>
     *
     * @param authenticated the new authentication state
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        if (authenticated) {
            failedAttempts = 0; // Reset on successful auth
        }
    }

    /**
     * Checks if the application lock feature is enabled.
     *
     * @return {@code true} if lock is enabled in settings, {@code false} otherwise
     */
    public boolean isLockEnabled() {
        return SettingsService.getInstance().getSettings().isLockEnabled();
    }

    // ==================== Authentication Operations ====================

    /**
     * Authenticates a user with the provided PIN.
     * 
     * @param pin The PIN to verify
     * @return true if authentication successful, false otherwise
     * @throws SecurityException if locked out due to too many failed attempts
     */
    public boolean authenticate(String pin) {
        // Check if currently locked out
        if (lockoutStartTime > 0) {
            long elapsedTime = System.currentTimeMillis() - lockoutStartTime;
            if (elapsedTime < LOCKOUT_DURATION_MS) {
                long remainingSeconds = (LOCKOUT_DURATION_MS - elapsedTime) / 1000;
                logger.warn("⚠️  Authentication locked out. {} seconds remaining.", remainingSeconds);
                throw new SecurityException(
                        "Too many authentication attempts. Please wait " + remainingSeconds + " seconds.");
            } else {
                // Lockout period has expired, reset
                lockoutStartTime = 0;
                failedAttempts = 0;
            }
        }

        String storedHash = SettingsService.getInstance().getSettings().getPinHash();
        if (storedHash == null || storedHash.isEmpty()) {
            authenticated = true;
            return true; // No PIN set
        }

        boolean success = false;

        // Try BCrypt verification first
        try {
            success = passwordEncoder.matches(pin, storedHash);
        } catch (IllegalArgumentException e) {
            // Hash doesn't look like BCrypt - might be old SHA-256 format
            // Allow one-time migration: check if it's the old format
            if (storedHash.length() == 64 && storedHash.matches("[a-fA-F0-9]{64}")) {
                // Old SHA-256 format detected
                String sha256Hash = hashWithSHA256(pin);
                if (sha256Hash.equals(storedHash)) {
                    success = true;
                    logger.warn("⚠️  Old PIN format detected. Please set a new PIN in Settings.");
                }
            }
        }

        if (success) {
            authenticated = true;
            failedAttempts = 0;
            lockoutStartTime = 0;
            logger.info("✓ Authentication successful");
        } else {
            failedAttempts++;
            logger.warn("✗ Authentication failed (attempt {})", failedAttempts);

            // Start lockout if max attempts reached
            if (failedAttempts >= MAX_FAILED_ATTEMPTS_BEFORE_DELAY) {
                lockoutStartTime = System.currentTimeMillis();
                logger.warn("⚠️  Too many failed attempts. Locking out for {} seconds.", LOCKOUT_DURATION_MS / 1000);
                throw new SecurityException("Too many authentication attempts. Please wait 60 seconds.");
            }
        }

        return success;
    }

    /**
     * Legacy SHA-256 hashing for migration support only.
     * DO NOT USE for new PINs - this is only for backward compatibility.
     */
    private String hashWithSHA256(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA-256 hash", e);
        }
    }

    /**
     * Sets a new PIN for the application lock.
     * Enforces minimum length requirement and uses BCrypt for hashing.
     * 
     * @param pin The new PIN to set
     * @throws IllegalArgumentException if PIN is too short
     */
    public void setPin(String pin) {
        if (pin == null || pin.length() < MIN_PIN_LENGTH) {
            throw new IllegalArgumentException(
                    "PIN must be at least " + MIN_PIN_LENGTH + " characters long for security. " +
                            "Consider using a combination of letters, numbers, and special characters.");
        }

        // Hash PIN with BCrypt (includes automatic salt generation)
        String hash = passwordEncoder.encode(pin);

        SettingsService.getInstance().getSettings().setPinHash(hash);
        SettingsService.getInstance().getSettings().setLockEnabled(true);
        SettingsService.getInstance().saveSettings();

        logger.info("✓ PIN set successfully with BCrypt hashing");
    }

    /**
     * Disables the application lock and auto-authenticates the current session.
     * <p>
     * After calling this method, {@link #isLockEnabled()} will return {@code false}
     * and {@link #isAuthenticated()} will return {@code true}.
     * </p>
     */
    public void disableLock() {
        SettingsService.getInstance().getSettings().setLockEnabled(false);
        SettingsService.getInstance().saveSettings();
        authenticated = true; // Auto-authenticate when lock is disabled
    }

    // ==================== Utility Methods ====================

    /**
     * Gets the number of failed authentication attempts since last success.
     *
     * @return the count of consecutive failed authentication attempts
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Validates a PIN format without checking against stored hash.
     * Used for pre-validation in UI before attempting authentication.
     */
    public static boolean isValidPinFormat(String pin) {
        return pin != null && pin.length() >= MIN_PIN_LENGTH;
    }

    /**
     * Gets the minimum required PIN length.
     */
    public static int getMinPinLength() {
        return MIN_PIN_LENGTH;
    }
}
