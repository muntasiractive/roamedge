package com.roam.constants;

/**
 * Application-wide constants for the Roam application.
 * <p>
 * This final utility class centralizes magic numbers, string literals, and
 * configuration
 * values used throughout the application. Constants are organized into logical
 * groups:
 * </p>
 * <ul>
 * <li>UI dimensions (window sizes, sidebar width)</li>
 * <li>Animation durations</li>
 * <li>Pagination settings</li>
 * <li>Database limits</li>
 * <li>File size limits</li>
 * <li>Date/time formats</li>
 * <li>Application information</li>
 * </ul>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public final class AppConstants {

    private AppConstants() {
        // prevent instantiation
    }

    // UI dimensions
    public static final int DEFAULT_WINDOW_WIDTH = 1024;
    public static final int DEFAULT_WINDOW_HEIGHT = 768;
    public static final int MIN_WINDOW_WIDTH = 800;
    public static final int MIN_WINDOW_HEIGHT = 600;
    public static final int SIDEBAR_WIDTH = 280;

    // animation durations in milliseconds
    public static final int ANIMATION_FAST = 150;
    public static final int ANIMATION_NORMAL = 250;
    public static final int ANIMATION_SLOW = 400;

    // pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int SEARCH_RESULTS_LIMIT = 50;

    // database limits
    public static final int MAX_TITLE_LENGTH = 255;
    public static final int MAX_DESCRIPTION_LENGTH = 5000;
    public static final int MAX_CONTENT_LENGTH = 50000;

    // file size limits (in bytes)
    public static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10 MB
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB

    // date/time formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";

    // application info
    public static final String APP_NAME = "Roam";
    public static final String APP_VERSION = "1.0.0";
}
