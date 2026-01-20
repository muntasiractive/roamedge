package com.roam.util;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Centralized theme manager for managing application-wide visual themes.
 * 
 * <p>
 * This singleton class manages the application's theme settings, supporting
 * multiple
 * AtlantaFX themes including light, dark, and system-aware options. It handles
 * theme
 * application to the main scene and individual dialogs/alerts.
 * </p>
 * 
 * <h2>Supported Themes</h2>
 * <ul>
 * <li><b>Light</b> - Primer Light theme (default)</li>
 * <li><b>Dark</b> - Primer Dark theme</li>
 * <li><b>System</b> - Automatically detects system preference (Windows)</li>
 * <li><b>Nord Light/Dark</b> - Nord color palette themes</li>
 * <li><b>Cupertino Light/Dark</b> - macOS-inspired themes</li>
 * <li><b>Dracula</b> - Popular dark theme</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Get singleton instance
 * ThemeManager themeManager = ThemeManager.getInstance();
 * 
 * // Set the main scene for theme management
 * themeManager.setMainScene(primaryStage.getScene());
 * 
 * // Apply a theme
 * themeManager.applyTheme("Dark");
 * 
 * // Style a dialog
 * themeManager.styleDialog(myDialog);
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class ThemeManager {

    // ========================================
    // CONSTANTS
    // ========================================

    /** Logger for theme management operations. */
    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);

    // ========================================
    // SINGLETON INSTANCE
    // ========================================

    /** Singleton instance of the ThemeManager. */
    private static ThemeManager instance;

    // ========================================
    // INSTANCE VARIABLES
    // ========================================

    /** The currently active theme name. */
    private String currentTheme = "Light";

    /** Reference to the main application scene. */
    private Scene mainScene;

    /** List of theme change listeners. */
    private final java.util.List<Runnable> listeners = new java.util.ArrayList<>();

    // ========================================
    // CONSTRUCTOR
    // ========================================

    /**
     * Private constructor for singleton pattern.
     * Use {@link #getInstance()} to obtain the singleton instance.
     */
    private ThemeManager() {
    }

    // ========================================
    // SINGLETON ACCESS
    // ========================================

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Set the main scene reference for theme management
     */
    public void setMainScene(Scene scene) {
        this.mainScene = scene;
        // Apply current theme to the scene
        applyDarkClass(scene.getRoot(), isDarkTheme());
    }

    /**
     * Adds a listener to be notified when the theme changes.
     * 
     * @param listener the listener to add
     */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Removes a theme change listener.
     * 
     * @param listener the listener to remove
     */
    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners that the theme has changed.
     */
    private void notifyListeners() {
        for (Runnable listener : listeners) {
            try {
                listener.run();
            } catch (Exception e) {
                logger.error("Error in theme listener", e);
            }
        }
    }

    // ========================================
    // THEME APPLICATION
    // ========================================

    /**
     * Applies the specified theme to the application.
     * 
     * <p>
     * This method sets the AtlantaFX user agent stylesheet and applies
     * the appropriate dark/light class to the main scene if it exists.
     * </p>
     * 
     * @param themeName the name of the theme to apply (case-insensitive)
     */
    public void applyTheme(String themeName) {
        this.currentTheme = themeName;

        switch (themeName.toLowerCase()) {
            case "dark":
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                break;
            case "system":
                // Detect system preference and apply appropriate theme
                boolean isSystemDark = isSystemDarkMode();
                if (isSystemDark) {
                    Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                } else {
                    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                }
                break;
            case "nord light":
                Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
                break;
            case "nord dark":
                Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
                break;
            case "cupertino light":
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
                break;
            case "cupertino dark":
                Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
                break;
            case "dracula":
                Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
                break;
            case "light":
            default:
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                break;
        }

        // Apply dark class to main scene if it exists
        if (mainScene != null) {
            applyDarkClass(mainScene.getRoot(), isDarkTheme());
        }

        notifyListeners();

        logger.info("✓ Applied AtlantaFX theme: {}", themeName);
    }

    // ========================================
    // THEME UTILITIES
    // ========================================

    /**
     * Applies or removes the "dark" CSS class from a parent node.
     * 
     * @param root   the parent node to modify
     * @param isDark whether to add (true) or remove (false) the dark class
     */
    public void applyDarkClass(Parent root, boolean isDark) {
        if (root == null)
            return;

        if (isDark) {
            if (!root.getStyleClass().contains("dark")) {
                root.getStyleClass().add("dark");
            }
        } else {
            root.getStyleClass().remove("dark");
        }
    }

    /**
     * Gets the name of the currently active theme.
     * 
     * @return the current theme name
     */
    public String getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Determines if the current theme is a dark theme.
     * 
     * <p>
     * For "System" theme, this checks the actual system preference.
     * For other themes, it checks if the theme name contains "dark" or is
     * "dracula".
     * </p>
     * 
     * @return true if the current theme is dark, false otherwise
     */
    public boolean isDarkTheme() {
        if (currentTheme.equalsIgnoreCase("system")) {
            return isSystemDarkMode();
        }
        return currentTheme.toLowerCase().contains("dark") ||
                currentTheme.equalsIgnoreCase("dracula");
    }

    // ========================================
    // SYSTEM DETECTION
    // ========================================

    /**
     * Detects if the operating system is using dark mode.
     * 
     * <p>
     * On Windows, this checks the registry for the AppsUseLightTheme setting.
     * For other operating systems, defaults to light mode.
     * </p>
     * 
     * @return true if the system is in dark mode, false otherwise
     */
    private boolean isSystemDarkMode() {
        try {
            // For Windows: Check registry for dark mode setting
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                ProcessBuilder pb = new ProcessBuilder(
                        "reg", "query",
                        "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                        "/v", "AppsUseLightTheme");
                Process process = pb.start();
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("AppsUseLightTheme")) {
                        // Value is 0x0 for dark mode, 0x1 for light mode
                        return line.contains("0x0");
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not detect system dark mode, defaulting to light: {}", e.getMessage());
        }
        return false; // Default to light mode
    }

    // ========================================
    // DIALOG STYLING
    // ========================================

    /**
     * Styles a dialog for the current theme.
     * 
     * <p>
     * This method applies the application stylesheet and dark class to the dialog.
     * For dark themes, it also sets explicit inline styles to ensure visibility,
     * as CSS variables don't work well in separate dialog windows.
     * </p>
     * 
     * @param dialog the dialog to style
     */
    public void styleDialog(Dialog<?> dialog) {
        if (dialog == null)
            return;

        DialogPane dialogPane = dialog.getDialogPane();

        // Add stylesheet
        try {
            String css = Objects.requireNonNull(
                    getClass().getResource("/styles/application.css")).toExternalForm();
            if (!dialogPane.getStylesheets().contains(css)) {
                dialogPane.getStylesheets().add(css);
            }
        } catch (Exception e) {
            logger.warn("Could not add stylesheet to dialog: {}", e.getMessage());
        }

        // Apply dark class if needed
        boolean isDark = isDarkTheme();
        applyDarkClass(dialogPane, isDark);

        // Set explicit inline styles for dark mode to ensure visibility
        // (CSS variables don't work well in separate dialog windows)
        if (isDark) {
            dialogPane.setStyle(
                    "-fx-background-color: #1e1e1e;");

            // Style header text
            if (dialogPane.getHeader() != null) {
                dialogPane.getHeader().setStyle("-fx-text-fill: #ffffff;");
            }
        } else {
            dialogPane.setStyle("");
        }
    }

    /**
     * Style an Alert for the current theme.
     */
    public void styleAlert(Alert alert) {
        styleDialog(alert);
    }

    /**
     * Create and style an Alert with proper theming.
     */
    public Alert createAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleAlert(alert);
        return alert;
    }

    /**
     * Create and style an Alert with just content.
     */
    public Alert createAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type);
        alert.setContentText(content);
        styleAlert(alert);
        return alert;
    }
}
