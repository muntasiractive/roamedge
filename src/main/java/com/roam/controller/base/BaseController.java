package com.roam.controller.base;

import com.roam.util.DialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base controller providing common functionality for all controllers.
 * <p>
 * This base class provides shared utilities including:
 * <ul>
 * <li>Centralized logging via SLF4J</li>
 * <li>Exception handling with user-friendly error dialogs</li>
 * <li>Confirmation dialog utilities</li>
 * </ul>
 * </p>
 * <p>
 * All concrete controllers should extend this class to leverage common
 * functionality
 * and maintain consistent error handling patterns across the application.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public abstract class BaseController {

    // ==================== Fields ====================

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // ==================== Error Handling Methods ====================

    /**
     * Handles exceptions by logging the error and displaying a user-friendly error
     * dialog.
     *
     * @param operation the name of the operation that failed
     * @param e         the exception that was thrown
     */
    protected void handleException(String operation, Exception e) {
        logger.error("{} failed: {}", operation, e.getMessage(), e);
        DialogUtils.showError(
                "Error",
                operation + " failed",
                e.getMessage());
    }

    // ==================== Dialog Methods ====================

    /**
     * Displays an error dialog to the user.
     *
     * @param title   the dialog title
     * @param header  the dialog header text
     * @param content the detailed error message
     */
    protected void showError(String title, String header, String content) {
        DialogUtils.showError(title, header, content);
    }

    /**
     * Displays a confirmation dialog and returns the user's response.
     *
     * @param title   the dialog title
     * @param header  the dialog header text
     * @param content the confirmation message
     * @return {@code true} if the user confirmed, {@code false} otherwise
     */
    protected boolean showConfirmation(String title, String header, String content) {
        return DialogUtils.showConfirmation(title, header, content);
    }
}
